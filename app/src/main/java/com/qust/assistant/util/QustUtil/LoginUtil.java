package com.qust.assistant.util.QustUtil;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.QustLoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

public class LoginUtil{
	
	private static final Pattern JESSIONID_PATTERN = Pattern.compile("JSESSIONID=(.*?);");
	
	@NonNull
	public static QustLoginResult login(String name, String password){
		return login(name, password, QustAPI.SEVER_HOSTS[0], new LoginProgress(){ });
	}
	
	@NonNull
	public static QustLoginResult login(String name, String password, String host){
		return login(name, password, host, new LoginProgress(){ });
	}
	
	@NonNull
	public static QustLoginResult login(String name, String password, @NonNull LoginProgress callback){
		return login(name, password, QustAPI.SEVER_HOSTS[0], callback);
	}
	
	@NonNull
	public static QustLoginResult login(String name, String password, String host, @NonNull LoginProgress callback){
		QustLoginResult result = new QustLoginResult();

		try{
			callback.onProgress("正在获取Cookie");

			String[] param = getLoginParam(host);
			if(param[0] == null || param[1] == null) throw new RuntimeException("服务器异常");
			
			callback.onProgress("正在获取RSA公钥");
			
			String key = getPublicKey(host, param[0]);
			if(key == null) throw new RuntimeException("服务器异常");
			
			String rsaPassword = encrypt(password, key);
			if(TextUtils.isEmpty(rsaPassword)) throw new RuntimeException("RSA加密出错");
			
			callback.onProgress("正在尝试登陆");
			
			HttpURLConnection connection = WebUtil.post(
					host + QustAPI.EA_LOGIN + "?time=" + System.currentTimeMillis(),
					"JSESSIONID=" + param[0],
					"language=zh_CN&csrftoken=" + param[1] + "&yhm=" + name + "&mm=" + URLEncoder.encode(rsaPassword, "utf-8")
			);
			
			switch(connection.getResponseCode()){
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case 307:
					Matcher matcher = JESSIONID_PATTERN.matcher(connection.getHeaderField("Set-Cookie"));
					result.cookie = matcher.find() ? matcher.group(1) : param[0];
					result.message = "登录成功";
					break;
					
				case HttpURLConnection.HTTP_OK:
				default:
					throw new RuntimeException("响应码错误");
			}
			
		}catch(IOException | JSONException | RuntimeException e){
			result.message = "登录失败" + e.getMessage();
		}
		return result;
	}

	
	
	/**
	 * 获取登录参数
	 * @return param[0] = [ Cookie | null ], param[1] = [ csrfToken | null ]
	 */
	@NonNull
	private static String[] getLoginParam(String host) throws IOException{
		String[] result = new String[2];
		
		HttpURLConnection connection = WebUtil.get(host + QustAPI.EA_LOGIN, null);
		
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
			
			String s = connection.getHeaderField("Set-Cookie");
			
			if(s == null) return result;
			
			Matcher matcher = JESSIONID_PATTERN.matcher(s);
			if(matcher.find()) result[0] = matcher.group(1);
			
			s = WebUtil.inputStream2string(connection.getInputStream());
			matcher = Pattern.compile("<input (.*?)>").matcher(s);
			while(matcher.find()){
				s = matcher.group();
				if(s.contains("csrftoken")){
					matcher = Pattern.compile("value=\"(.*?)\"").matcher(s);
					if(matcher.find()) result[1] = matcher.group(1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取RSA公钥
	 */
	@Nullable
	private static String getPublicKey(String host, String cookie) throws IOException, JSONException{
		String str = WebUtil.doGet(
				host + QustAPI.EA_LOGIN_PUBLIC_KEY,
				"JSESSIONID=" + cookie
		);
		if(str.length() == 0){
			return null;
		}else{
			return new JSONObject(str).getString("modulus");
		}
	}
	
	/**
	 * RSA公钥加密
	 */
	@Nullable
	private static String encrypt(String str, String publicKey){
		try{
			// base64编码的公钥
			byte[] decoded = Base64.decode(publicKey, Base64.DEFAULT);
			StringBuilder sb = new StringBuilder();
			for(byte b : decoded){
				String hex = Integer.toHexString(b & 0xFF);
				if(hex.length() < 2) sb.append(0);
				sb.append(hex);
			}
			BigInteger a = new BigInteger(sb.toString(), 16);
			BigInteger b = new BigInteger("65537");
			RSAPublicKey pubKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(a, b));
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return Base64.encodeToString(cipher.doFinal(str.getBytes()), Base64.DEFAULT);
		}catch(Exception e){
			return null;
		}
	}
	
	public interface LoginProgress{
		default void onProgress(String msg){ }
	}
}
