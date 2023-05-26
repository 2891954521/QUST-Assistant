package com.qust.assistant.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.base.QFragmentActivity;
import com.qust.assistant.ui.fragment.school.LoginFragment;
import com.qust.assistant.util.SettingUtil;
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
	
	/**
	 * 教务服务器
	 */
	public String host;
	
	/**
	 * 学号
	 */
	public String name;
	
	/**
	 * 密码
	 */
	private String password;
	
	private MutableLiveData<QustLoginResult> loginResult;
	
	public LoginViewModel(@NonNull Application application){
		super(application);
		loginResult = new MutableLiveData<>(new QustLoginResult(null, null, null));
		
		host = SEVER_HOSTS[0];
		
		name = SettingUtil.getString(application.getString(R.string.SCHOOL_NAME), null);
		password = SettingUtil.getString(application.getString(R.string.SCHOOL_PASSWORD), null);
	}
	
	public static LoginViewModel getInstance(@NonNull Context context){
		return ((App)context.getApplicationContext()).loginViewModel;
	}
	
	public String getCookie(){
		return loginResult.getValue().cookie;
	}
	
	public MutableLiveData<QustLoginResult> getLoginResult(){
		return loginResult;
	}
	
	
	/**
	 * 异步登录请求, 使用储存的用户名和密码
	 */
	public synchronized void login(QFragmentActivity activity, @NonNull Handler handler){
		handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在检查登陆状态"));
		try{
			if(checkLogin()){
				postValue(handler, "登录成功", loginResult.getValue().cookie);
				return;
			}
		}catch(IOException e){
			postValue(handler, "登录失败" + e.getMessage(), null);
		}

		if(name == null || password == null){
			activity.runOnUiThread(() -> activity.addView(LoginFragment.class));
		}else{
			login(handler, name, password);
		}
	}
	
	/**
	 * 异步登录请求
	 * @param name 		用户名
	 * @param password	密码
	 */
	public synchronized void login(@NonNull Handler handler, String name, String password){
		try{
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在获取Cookie"));
			
			String[] param = getLoginParam();
			if(param[0] == null || param[1] == null){
				postValue(handler, "登陆失败，服务器异常", null);
				return;
			}
			
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在获取RSA公钥"));
			
			String key = getPublicKey(param[0]);
			if(key == null){
				postValue(handler, "登陆失败，服务器异常", null);
				return;
			}
			
			String rsaPassword = encrypt(password, key);
			if(TextUtils.isEmpty(rsaPassword)){
				postValue(handler, "登陆失败，RSA加密出错", null);
				return;
			}
			
			handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在尝试登陆"));
			
			HttpURLConnection connection = WebUtil.post(
					host + "/jwglxt/xtgl/login_slogin.html?time=" + System.currentTimeMillis(),
					"JSESSIONID=" + param[0],
					"csrftoken=" + param[1] + "&language=zh_CN&yhm=" + name + "&mm=" + URLEncoder.encode(rsaPassword, "utf-8")
			);
			
			switch(connection.getResponseCode()){
				case HttpURLConnection.HTTP_OK:
					postValue(handler, "登陆失败，用户名或密码错误", null);
					break;
				
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case 307:
					SettingUtil.edit()
							.putString(getApplication().getString(R.string.SCHOOL_NAME), name)
							.putString(getApplication().getString(R.string.SCHOOL_PASSWORD), password).apply();
					
					Matcher matcher = JESSIONID_PATTERN.matcher(connection.getHeaderField("Set-Cookie"));
					postValue(handler, "登陆成功", matcher.find() ? matcher.group(1) : param[0]);
					break;
		
				default:
					postValue(handler, "登陆失败，未知的响应码", null);
			}
		}catch(IOException e){
			postValue(handler, "登陆失败" + e.getMessage(), null);
		}
	}
	
	/**
	 * 同步登录请求，使用储存的用户名和密码，直接返还Cookie，自动更新ViewModel里的Cookie
	 * @return Cookie
	 */
	public synchronized String login(){
		try{
			if(checkLogin()) return getCookie();
		}catch(IOException ignored){ }
		
		if(name == null || password == null){
			return null;
		}else{
			String cookie = login(name, password);
			postValue(null, null, cookie);
			return cookie;
		}
	}
	
	/**
	 * 同步登录请求，直接返还Cookie，不更新ViewModel里的Cookie
	 * @param name 用户名
	 * @param password 密码
	 * @return Cookie | null
	 */
	public synchronized String login(String name, String password){
		try{
			String[] param = getLoginParam();
			if(param[0] == null || param[1] == null) return null;
			
			String key = getPublicKey(param[0]);
			if(key == null) return null;
			
			String rsaPassword = encrypt(password, key);
			if(TextUtils.isEmpty(rsaPassword)) return null;
			
			HttpURLConnection connection = WebUtil.post(
					host + "/jwglxt/xtgl/login_slogin.html?time=" + System.currentTimeMillis(),
					"JSESSIONID=" + param[0],
					"csrftoken=" + param[1] + "&language=zh_CN&yhm=" + name + "&mm=" + URLEncoder.encode(rsaPassword, "utf-8")
			);
			
			switch(connection.getResponseCode()){
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case 307:
					SettingUtil.edit()
							.putString(getApplication().getString(R.string.SCHOOL_NAME), name)
							.putString(getApplication().getString(R.string.SCHOOL_PASSWORD), password).apply();
					
					Matcher matcher = JESSIONID_PATTERN.matcher(connection.getHeaderField("Set-Cookie"));
					return matcher.find() ? matcher.group(1) : param[0];
					
				case HttpURLConnection.HTTP_OK:
				default:
					return null;
			}
		}catch(IOException e){
			return null;
		}
	}
	
	
	/**
	 * GET请求，携带Cookie
	 * @param url 不带host的url
	 */
	public String doGet(String url) throws IOException{
		return WebUtil.doGet(
				host + url,
				"JSESSIONID=" + getCookie()
		);
	}
	
	/**
	 * POST请求，携带Cookie
	 * @param url 不带host的url
	 * @param data post数据
	 */
	public String doPost(String url, String data) throws IOException{
		return WebUtil.doPost(
				host + url,
				"JSESSIONID=" + getCookie(),
				data
		);
	}
	
	/**
	 * 检查Cookie是否有效
	 */
	private boolean checkLogin() throws IOException{
		if(loginResult.getValue() == null) return false;
		HttpURLConnection connection = WebUtil.get(host + "/jwglxt/xtgl/index_initMenu.html", "JSESSIONID=" + loginResult.getValue().cookie);
		return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
	}
	
	private void postValue(Handler handler, String message, String cookie){
		loginResult.postValue(new QustLoginResult(handler, message, cookie));
	}
	
	/**
	 * 获取登录参数
	 * @return param[0] = [ Cookie | null ], param[1] = [ csrfToken | null ]
	 */
	@NonNull
	private String[] getLoginParam() throws IOException{
		String[] result = new String[2];
		
		HttpURLConnection connection = WebUtil.get(host + "/jwglxt/xtgl/login_slogin.html", null);
		
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
	private String getPublicKey(String cookie){
		try{
			String str = WebUtil.doGet(
					host + "/jwglxt/xtgl/login_getPublicKey.html",
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
