package com.qust.account.ea;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qust.QustAPI;
import com.qust.account.AccountViewModel;
import com.qust.account.RequestCallback;
import com.qust.account.RequestErrorCallback;
import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 教务系统登录模块
 */
public class EAViewModel extends AccountViewModel{
	
	private static volatile EAViewModel INSTANCE;
	
	
	/**
	 * 入学年份信息
	 */
	protected MutableLiveData<Integer> entranceTimeData;
	
	
	
	public static EAViewModel getInstance(Context context){
		if(INSTANCE == null){
			synchronized(EAViewModel.class){
				if(INSTANCE == null){
					INSTANCE = new EAViewModel((Application)context.getApplicationContext());
				}
			}
		}
		return INSTANCE;
	}
	
	public EAViewModel(@NonNull Application application){
		super(application);
		entranceTimeData = new MutableLiveData<>(SettingUtil.getInt(application.getString(R.string.KEY_ENTRANCE_TIME), -1));
		
		Resources resources = application.getResources();
		init("https", QustAPI.EA_HOSTS[SettingUtil.getInt(resources.getString(R.string.KEY_EA_HOST), 0)],
				resources.getString(R.string.EA_NAME),
				resources.getString(R.string.EA_PASSWORD),
				resources.getString(R.string.EA_COOKIE)
		);
	}
	
	@NonNull
	public LiveData<Integer> getEntranceTimeData(){
		return entranceTimeData;
	}
	
	public int getEntranceTime(){
		Integer val = entranceTimeData.getValue();
		return (val == null) ? -1 : val;
	}
	
	public void setEntranceTime(int entranceTime){
		SettingUtil.edit().putInt(getApplication().getString(R.string.KEY_ENTRANCE_TIME), 0).commit();
		entranceTimeData.postValue(entranceTime);
	}
	
	public void changeEAHost(int index){
		if(index >= QustAPI.EA_HOSTS.length){
			return;
		}
		
		isLogin = false;
		
		Resources resources = getApplication().getResources();
		
		String cookieName = resources.getString(R.string.EA_COOKIE);
		
		SettingUtil.edit()
				.putStringSet(cookieName, new HashSet<>())
				.putInt(resources.getString(R.string.KEY_EA_HOST), index)
				.commit();
		
		init("https", QustAPI.EA_HOSTS[index],
				resources.getString(R.string.EA_NAME),
				resources.getString(R.string.EA_PASSWORD),
				cookieName
		);
	}
	
	
	@Override
	public synchronized void checkLoginAsync(RequestCallback callback, RequestErrorCallback errorCallback){
		clientNoFollowRedirects.newCall(new Request.Builder().url(host + "jwglxt/xtgl/index_initMenu.html").build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				if(response.code() == HttpURLConnection.HTTP_OK){
					isLogin = true;
					loginData.postValue(true);
					callback.onSuccess(response, null);
				}else if(loginSync()){
					callback.onSuccess(response, null);
				}else{
					errorCallback.onNeedLogin();
				}
			}
		});
	}
	
	@Override
	public synchronized void loginAsync(String userName, String password, RequestCallback callback, RequestErrorCallback errorCallback){
		cookieJar.clearCookies();
		
		getWithOutCheck(QustAPI.EA_LOGIN, (resp, html) -> {
			String publicKey;
			String csrfToken = null;
			
			if(html == null) errorCallback.onNetworkError(new IOException("网络错误"));
			
			Matcher matcher = Pattern.compile("<input (.*?)>").matcher(html);
			while(matcher.find()){
				csrfToken = matcher.group();
				if(csrfToken.contains("csrftoken")){
					matcher = Pattern.compile("value=\"(.*?)\"").matcher(csrfToken);
					if(matcher.find()) csrfToken = matcher.group(1);
					break;
				}
			}
			
			if(csrfToken == null) errorCallback.onNetworkError(new IOException("无法获取 csrfToken"));
			
			try(Response response = getWithOutCheck(QustAPI.EA_LOGIN_PUBLIC_KEY)){
				publicKey = new JSONObject(response.body().string()).getString("modulus");
			}catch(JSONException e){
				errorCallback.onNetworkError(new IOException("无法获取 publicKey"));
				return;
			}
			
			String rsaPassword = encrypt(password, publicKey);
			if(TextUtils.isEmpty(rsaPassword)){
				errorCallback.onNetworkError(new IOException("RSA加密出错"));
				return;
			}
			
			try(Response response = clientNoFollowRedirects.newCall(new Request.Builder().url(host + QustAPI.EA_LOGIN).post(
					new FormBody.Builder()
							.add("csrftoken", csrfToken)
							.add("yhm", userName)
							.add("mm", rsaPassword)
							.build()).build()).execute()
			){
				int code = response.code();
				if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307){
					SettingUtil.edit().putString(accountName, userName).putString(passwordName, password).apply();
					isLogin = true;
					loginData.postValue(true);
					callback.onSuccess(response, null);
				}else{
					isLogin = false;
					errorCallback.onNeedLogin();
				}
			}
			
		}, errorCallback);
	}
	
	@Override
	public synchronized boolean checkLoginSync() throws IOException{
		try(Response response = clientNoFollowRedirects.newCall(new Request.Builder().url(host + "jwglxt/xtgl/index_initMenu.html").build()).execute()){
			if(response.code() == HttpURLConnection.HTTP_OK){
				isLogin = true;
				loginData.postValue(true);
				return true;
			}else return loginSync();
		}
	}
	
	@Override
	public synchronized boolean loginSync(String userName, String password) throws IOException{
		cookieJar.clearCookies();
		
		String publicKey;
		String csrfToken = null;
		
		try(Response response = getWithOutCheck(QustAPI.EA_LOGIN)){
			String html = response.body().string();
			
			Matcher matcher = Pattern.compile("<input (.*?)>").matcher(html);
			while(matcher.find()){
				csrfToken = matcher.group();
				if(csrfToken.contains("csrftoken")){
					matcher = Pattern.compile("value=\"(.*?)\"").matcher(csrfToken);
					if(matcher.find()) csrfToken = matcher.group(1);
					break;
				}
			}
		}
		if(csrfToken == null) throw new IOException("无法获取 csrfToken");
		
		try(Response response = getWithOutCheck(QustAPI.EA_LOGIN_PUBLIC_KEY)){
			publicKey = new JSONObject(response.body().string()).getString("modulus");
		}catch(JSONException e){
			throw new IOException("无法获取 publicKey");
		}
		
		String rsaPassword = encrypt(password, publicKey);
		if(TextUtils.isEmpty(rsaPassword)) throw new IOException("RSA加密出错");
		
		try(Response response = clientNoFollowRedirects.newCall(new Request.Builder().url(host + QustAPI.EA_LOGIN).post(
				new FormBody.Builder()
					.add("csrftoken", csrfToken)
					.add("yhm", userName)
					.add("mm", rsaPassword)
					.build()).build()).execute()
		){
			int code = response.code();
			if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307){
				SettingUtil.edit().putString(accountName, userName).putString(passwordName, password).apply();
				isLogin = true;
				loginData.postValue(true);
				return true;
			}else{
				isLogin = false;
				return false;
			}
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
	
}
