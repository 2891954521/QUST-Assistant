package com.university.assistant.util;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import androidx.annotation.Nullable;

public class LoginUtil{
	
	@Nullable
	public static String login(String name,String password){
		String[] param = getJSESSIONID();
		if(param[0]==null||param[1]==null){
			return "登陆失败！服务器异常！";
		}
		String key = getPublicKey(param[0]);
		if(key==null){
			return "登陆失败！服务器异常！";
		}
		String rsaPassword = encrypt(password,key);
		if(rsaPassword==null){
			return "登陆失败！RSA加密出错！";
		}
		try{
			HttpURLConnection connection = WebUtil.post(
					"http://jwglxt.qust.edu.cn/jwglxt/xtgl/login_slogin.html?time=" + System.currentTimeMillis(),
					"JSESSIONID=" + param[0],
					"csrftoken=" + param[1] + "&language=zh_CN&yhm=" + name + "&mm=" + URLEncoder.encode(rsaPassword,"utf-8")
			);
			if(connection.getResponseCode()==HttpURLConnection.HTTP_MOVED_TEMP || connection.getResponseCode()==HttpURLConnection.HTTP_OK){
				String s = connection.getHeaderField("Set-Cookie");
				Matcher matcher = Pattern.compile("JSESSIONID=(.*?);").matcher(s);
				if(matcher.find()){
					return "=" + matcher.group(1);
				}
			}
		}catch(IOException e){
			LogUtil.Log(e);
		}
		return null;
	}
	
	private static String[] getJSESSIONID(){
		String[] result = new String[2];
		String html = null;
		try{
			HttpURLConnection connection = WebUtil.get("http://jwglxt.qust.edu.cn/jwglxt/xtgl/login_slogin.html","");
			if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
				String s = connection.getHeaderField("Set-Cookie");
				if(s != null){
					Pattern pattern = Pattern.compile("JSESSIONID=(.*?);");
					Matcher matcher = pattern.matcher(s);
					if(matcher.find()){
						result[0] = matcher.group(1);
					}
					html = WebUtil.inputStream2string(connection.getInputStream());
				}
			}
		}catch(IOException e){
			LogUtil.Log(e);
			html = null;
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
	
	@Nullable
	private static String getPublicKey(String JSESSIONID){
		try{
			String str = WebUtil.doGet(
					"http://jwglxt.qust.edu.cn/jwglxt/xtgl/login_getPublicKey.html",
					"JSESSIONID=" + JSESSIONID
			);
			if(str == null) return null;
			return new JSONObject(str).getString("modulus");
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return null;
	}
	
	/**
	 * RSA公钥加密
	 */
	@Nullable
	private static String encrypt(String str,String publicKey){
		try{
			// base64编码的公钥
			byte[] decoded = Base64.decode(publicKey,Base64.DEFAULT);
			StringBuilder sb = new StringBuilder();
			for(byte b : decoded){
				String hex = Integer.toHexString(b & 0xFF);
				if(hex.length()<2) sb.append(0);
				sb.append(hex);
			}
			BigInteger a = new BigInteger(sb.toString(),16);
			BigInteger b = new BigInteger("65537");
			RSAPublicKey pubKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(a,b));
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE,pubKey);
			return Base64.encodeToString(cipher.doFinal(str.getBytes()),Base64.DEFAULT);
		}catch(Exception e){
			LogUtil.Log(e);
			return null;
		}
	}
}
