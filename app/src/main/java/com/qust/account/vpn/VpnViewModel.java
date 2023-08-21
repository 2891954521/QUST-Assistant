package com.qust.account.vpn;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.qust.QustAPI;
import com.qust.account.AccountViewModel;
import com.qust.account.RequestCallback;
import com.qust.account.RequestErrorCallback;
import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * QUST VPN登录模块
 */
public class VpnViewModel extends AccountViewModel{
	
	private static volatile VpnViewModel INSTANCE;
	
	private static final Pattern LT_PATTERN = Pattern.compile("name=\"lt\" value=\"(LT-\\d+-[a-zA-Z\\d]+-tpass)\"");
	
	public static VpnViewModel getInstance(Context context){
		if(INSTANCE == null){
			synchronized(VpnViewModel.class){
				if(INSTANCE == null){
					INSTANCE = new VpnViewModel((Application)context.getApplicationContext());
				}
			}
		}
		return INSTANCE;
	}
	
	public VpnViewModel(@NonNull Application application){
		super(application);
		Resources resources = application.getResources();
		init("https", QustAPI.VPN_HOST,
				resources.getString(R.string.VPN_NAME),
				resources.getString(R.string.VPN_PASSWORD),
				resources.getString(R.string.VPN_Cookie)
		);
	}
	
	/**
	 * 异步检查登陆状态，有账号信息时自动进行登录
	 * @see #loginAsync(RequestCallback, RequestErrorCallback)
	 */
	public synchronized void checkLoginAsync(RequestCallback callback, RequestErrorCallback errorCallback){
		clientNoFollowRedirects.newCall(new Request.Builder().url(QustAPI.VPN_LOGIN).build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				int code = response.code();
				if((code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) && response.header("Location").contains("login")){
					try{
						if(loginSync()) callback.onSuccess(response, null);
						else errorCallback.onNeedLogin();
					}catch(IOException e){
						errorCallback.onNetworkError(e);
					}
				}else{
					isLogin = true;
					loginData.postValue(true);
					callback.onSuccess(response, null);
				}
			}
		});
	}
	
	
	/**
	 * 异步登录请求
	 * @param callback 登录成功时的回调
	 * @param errorCallback 网络错误或者用户名或密码错误时的回调
	 */
	public synchronized void loginAsync(String userName, String password, RequestCallback callback, RequestErrorCallback errorCallback){
		cookieJar.clearCookies();
		okHttpClient.newCall(new Request.Builder().url(QustAPI.VPN_LOGIN).build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				String lt;
				
				String html = response.body().string();
				Matcher matcher = LT_PATTERN.matcher(html);
				if(matcher.find()){
					lt = matcher.group(1);
				}else{
					errorCallback.onNetworkError(new IOException("无法获取lt"));
					return;
				}
				String secret = VpnEncodeUtil.encode(userName, password, lt);
				okHttpClient.newCall(new Request.Builder().url(response.request().url()).post(new FormBody.Builder()
						.add("rsa", secret)
						.add("ul", String.valueOf(userName.length()))
						.add("pl", String.valueOf(password.length()))
						.add("lt", lt)
						.add("execution", "e1s1")
						.add("_eventId", "submit")
						.build()).build()).enqueue(new Callback(){
					@Override
					public void onFailure(@NonNull Call call, @NonNull IOException e){
						errorCallback.onNetworkError(e);
					}
					
					@Override
					public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
						Response lastResponse = response.priorResponse();
						if(lastResponse != null){
							int priorResponseCode = lastResponse.code();
							if(priorResponseCode == HttpURLConnection.HTTP_MOVED_PERM || priorResponseCode == HttpURLConnection.HTTP_MOVED_TEMP){
								SettingUtil.edit().putString(accountName, userName).putString(passwordName, password).apply();
								isLogin = true;
								loginData.postValue(true);
								callback.onSuccess(response, null);
							}else{
								errorCallback.onNeedLogin();
							}
						}else{
							errorCallback.onNeedLogin();
						}
					}
				});
			}
		});
	}
	
	/**
	 * 同步检查登陆状态，有账号信息时自动进行登录
	 * @see #loginSync()
	 */
	public synchronized boolean checkLoginSync() throws IOException{
		try(Response response = clientNoFollowRedirects.newCall(new Request.Builder().url(QustAPI.VPN_LOGIN).build()).execute()){
			int code = response.code();
			if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP){
				return !response.header("Location").contains("login") || loginSync();
			}else{
				isLogin = true;
				loginData.postValue(true);
				return true;
			}
		}
	}
	
	/**
	 * 同步登录请求
	 * @return 登录是否成功（失败的情况下为账号或密码错误）
	 * @throws IOException 网络错误
	 */
	public synchronized boolean loginSync(String userName, String password) throws IOException{
		cookieJar.clearCookies();

		String lt;
		HttpUrl loginUrl;
		
		try(Response response = okHttpClient.newCall(
				new Request.Builder()
						.url(QustAPI.VPN_LOGIN)
						.build()
		).execute()){
			String html = response.body().string();
			Matcher matcher = LT_PATTERN.matcher(html);
			if(matcher.find()){
				lt = matcher.group(1);
			}else{
				throw new IOException("无法获取lt");
			}
			loginUrl = response.request().url();
		}
		
		String secret = VpnEncodeUtil.encode(userName, password, lt);
		
		try(Response response = okHttpClient.newCall(
				new Request.Builder().url(loginUrl)
						.post(new FormBody.Builder()
								.add("rsa", secret)
								.add("ul", String.valueOf(userName.length()))
								.add("pl", String.valueOf(password.length()))
								.add("lt", lt)
								.add("execution", "e1s1")
								.add("_eventId", "submit")
								.build())
						.build()
		).execute()){
			Response lastResponse = response.priorResponse();
			if(lastResponse != null){
				int priorResponseCode = lastResponse.code();
				if(priorResponseCode == HttpURLConnection.HTTP_MOVED_PERM || priorResponseCode == HttpURLConnection.HTTP_MOVED_TEMP){
					SettingUtil.edit().putString(accountName, userName).putString(passwordName, password).apply();
					isLogin = true;
					loginData.postValue(true);
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
	}
	
}
