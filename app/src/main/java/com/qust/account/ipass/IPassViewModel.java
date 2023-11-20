package com.qust.account.ipass;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.qust.QustAPI;
import com.qust.account.AccountViewModel;
import com.qust.account.RequestCallback;
import com.qust.account.RequestErrorCallback;
import com.qust.account.vpn.VpnEncodeUtil;
import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class IPassViewModel extends AccountViewModel{
	
	private static volatile IPassViewModel INSTANCE;
	
	public static IPassViewModel getInstance(Context context){
		if(INSTANCE == null){
			synchronized(IPassViewModel.class){
				if(INSTANCE == null){
					INSTANCE = new IPassViewModel((Application)context.getApplicationContext());
				}
			}
		}
		return INSTANCE;
	}
	
	public IPassViewModel(@NonNull Application application){
		super(application);
		Resources resources = application.getResources();
		init("https", QustAPI.IPASS_HOST,
				resources.getString(R.string.VPN_NAME),
				resources.getString(R.string.VPN_PASSWORD),
				resources.getString(R.string.IPass_Cookie)
		);
	}
	
	@Override
	public void checkLoginAsync(RequestCallback callback, RequestErrorCallback errorCallback){
		clientNoFollowRedirects.newCall(new Request.Builder().url(host + "tp_up/view?m=up").build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				int code = response.code();
				if(code == HttpURLConnection.HTTP_OK){
					isLogin = true;
					loginData.postValue(true);
					callback.onSuccess(response, null);
				}else if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP){
					if(response.header("Location").contains("login")){
						if(loginSync()){
							callback.onSuccess(response, null);
						}else{
							errorCallback.onNeedLogin();
						}
					}else{
						callback.onSuccess(response, null);
					}
				}else{
					errorCallback.onNeedLogin();
				}
			}
		});
	}
	
	@Override
	public void loginAsync(String userName, String password, RequestCallback callback, RequestErrorCallback errorCallback){
		cookieJar.clearCookies();
		okHttpClient.newCall(new Request.Builder().url(QustAPI.IPASS_LOGIN).build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				String lt;
				
				String html = response.body().string();
				Matcher matcher = VpnEncodeUtil.LT_PATTERN.matcher(html);
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
	
	@Override
	public boolean checkLoginSync() throws IOException{
		try(Response response = clientNoFollowRedirects.newCall(new Request.Builder().url(host + "tp_up/view?m=up").build()).execute()){
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
	
	@Override
	public boolean loginSync(String userName, String password) throws IOException{
		cookieJar.clearCookies();
		
		String lt;
		HttpUrl loginUrl;
		
		try(Response response = okHttpClient.newCall(new Request.Builder().url(QustAPI.IPASS_LOGIN).build()).execute()){
			String html = response.body().string();
			Matcher matcher = VpnEncodeUtil.LT_PATTERN.matcher(html);
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
