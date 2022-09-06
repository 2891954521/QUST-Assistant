package com.qust.assistant.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.App;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;

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

public class LoginViewModel extends AndroidViewModel{
	
	public static final String[] SEVER_HOSTS = {
			"https://jwglxt.qust.edu.cn",
			"https://jwglxt1.qust.edu.cn",
			"https://jwglxt2.qust.edu.cn",
			"https://jwglxt3.qust.edu.cn",
			"https://jwglxt4.qust.edu.cn",
			"https://jwglxt5.qust.edu.cn",
			"https://jwglxt6.qust.edu.cn",
	};
	
	public static final Pattern JESSIONID_PATTERN = Pattern.compile("JSESSIONID=(.*?);");
	
	public String HOST;
	
	private MutableLiveData<String> cookie;
	
	public LoginViewModel(@NonNull Application application){
		super(application);
		cookie = new MutableLiveData<>();
		HOST = SEVER_HOSTS[0];
	}
	
	public static LoginViewModel getInstance(@NonNull Context context){
		return ((App)context.getApplicationContext()).loginViewModel;
	}
	
	public MutableLiveData<String> getCookieLiveData(){
		return cookie;
	}
	
	public String getCookie(){
		return cookie.getValue();
	}
	
	/**
	 * 教务登录
	 * @return 错误消息
	 */
	@Nullable
	public String login(Handler handler, String name, String password){
		try{
			if(cookie.getValue() != null){
				
				handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在检查登陆状态"));
				
				HttpURLConnection connection = WebUtil.get(
						HOST + "/jwglxt/xtgl/index_initMenu.html",
						"JSESSIONID=" + cookie.getValue());
				if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
					return null;
				}
			}
			
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在获取Cookie"));
			
			String[] param = getLoginParam();
			if(param[0] == null || param[1] == null){
				return "登陆失败！服务器异常！";
			}
			
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在获取RSA公钥"));
			
			String key = getPublicKey(param[0]);
			if(key == null){
				return "登陆失败！服务器异常！";
			}
			
			String rsaPassword = encrypt(password, key);
			if(rsaPassword == null){
				return "登陆失败！RSA加密出错！";
			}
			
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在尝试登陆"));
			
			HttpURLConnection connection = WebUtil.post(
					HOST + "/jwglxt/xtgl/login_slogin.html?time=" + System.currentTimeMillis(),
					"JSESSIONID=" + param[0],
					"csrftoken=" + param[1] + "&language=zh_CN&yhm=" + name + "&mm=" + URLEncoder.encode(rsaPassword, "utf-8")
			);
			
			switch(connection.getResponseCode()){
				case HttpURLConnection.HTTP_OK:
					return "用户名或密码错误";
				
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case 307:
					Matcher matcher = JESSIONID_PATTERN.matcher(connection.getHeaderField("Set-Cookie"));
					cookie.postValue(matcher.find() ? matcher.group(1) : param[0]);
					return null;
				
				default:
					return "登陆失败";
			}
		}catch(IOException e){
			return "登陆失败!";
		}
	}
	
	/**
	 * 获取登录参数
	 */
	@NonNull
	private String[] getLoginParam(){
		String[] result = new String[2];
		String html = null;
		try{
			HttpURLConnection connection = WebUtil.get(HOST + "/jwglxt/xtgl/login_slogin.html", null);
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
				String s = connection.getHeaderField("Set-Cookie");
				if(s != null){
					Matcher matcher = JESSIONID_PATTERN.matcher(s);
					if(matcher.find()){
						result[0] = matcher.group(1);
					}
					html = WebUtil.inputStream2string(connection.getInputStream());
				}
			}
		}catch(IOException e){
			LogUtil.Log(e);
		}
		if(html != null){
			Matcher matcher = Pattern.compile("<input (.*?)>").matcher(html);
			while(matcher.find()){
				String s = matcher.group();
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
	private String getPublicKey(String cookie){
		try{
			String str = WebUtil.doGet(
					HOST + "/jwglxt/xtgl/login_getPublicKey.html",
					"JSESSIONID=" + cookie
			);
			if(str.length() == 0) return null;
			return new JSONObject(str).getString("modulus");
		}catch(IOException | JSONException ignored){
		}
		return null;
	}
	
	/**
	 * RSA公钥加密
	 */
	@Nullable
	private String encrypt(String str, String publicKey){
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
}
