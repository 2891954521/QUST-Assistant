package com.qust.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.util.SettingUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 自动处理账号登录状态
 */
public abstract class AccountViewModel extends AndroidViewModel{
	
	/**
	 * Cookie是否有效
	 */
	protected boolean isLogin;
	
	protected String host, accountName, passwordName;
	
	/**
	 * HTTP请求对象
	 */
	protected OkHttpClient okHttpClient;
	
	/**
	 * HTTP请求对象，不跟随重定向
	 */
	protected OkHttpClient clientNoFollowRedirects;
	
	protected AccountCookieJar cookieJar;
	
	/**
	 * 登录成功时发送事件
	 */
	protected MutableLiveData<Boolean> loginData;
	
	
	public AccountViewModel(@NonNull Application application){
		super(application);
		loginData = new MutableLiveData<>(false);
	}
	
	public void init(String scheme, String host, String accountName, String passwordName, String cookieName){
		this.accountName = accountName;
		this.passwordName = passwordName;
		
		cookieJar = new AccountCookieJar(host, scheme, cookieName);
		okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build();
		clientNoFollowRedirects = new OkHttpClient.Builder().cookieJar(cookieJar).followRedirects(false).build();
		
		this.host = scheme + "://" + host + "/";
	}
	
	
	public boolean isLogin(){
		return isLogin;
	}
	
	public LiveData<Boolean> getLoginData(){
		return loginData;
	}
	
	
	/**
	 * 异步GET请求
	 * @param url 不带HOST的URL
	 * @param callback 请求成功时的回调
	 * @param errorCallback 请求错误时的回调
	 */
	public void get(String url, RequestCallback callback, RequestErrorCallback errorCallback){
		requestWithCheck(new Request.Builder().url(host + url).build(), callback, errorCallback);
	}
	
	/**
	 * 异步POST请求
	 * @see #get(String, RequestCallback, RequestErrorCallback)
	 */
	public void post(String url, RequestBody requestBody, RequestCallback callback, RequestErrorCallback errorCallback){
		requestWithCheck(new Request.Builder().url(host + url).post(requestBody).build(), callback, errorCallback);
	}
	
	public void getWithOutCheck(String url, RequestCallback callback, RequestErrorCallback errorCallback){
		okHttpClient.newCall(new Request.Builder().url(host + url).build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				ResponseBody body = response.body();
				callback.onSuccess(response, body == null ? null : body.string());
			}
		});
	}
	
	public void postWithOutCheck(String url, RequestBody requestBody, RequestCallback callback, RequestErrorCallback errorCallback){
		okHttpClient.newCall(new Request.Builder().url(host + url).post(requestBody).build()).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				ResponseBody body = response.body();
				callback.onSuccess(response, body == null ? null : body.string());
			}
		});
	}
	
	
	/**
	 * 先检查登录状态再进行HTTP请求
	 */
	private void requestWithCheck(Request request, RequestCallback callback, RequestErrorCallback errorCallback){
		if(isLogin){
			request(request, callback, errorCallback);
		}else{
			checkLoginAsync((response, html) -> request(request, callback, errorCallback), errorCallback);
		}
	}
	
	/**
	 * HTTP请求
	 */
	protected void request(Request request, RequestCallback callback, RequestErrorCallback errorCallback){
		okHttpClient.newCall(request).enqueue(new Callback(){
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e){
				errorCallback.onNetworkError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
				Response lastResponse = response.priorResponse();
				if(lastResponse != null && (lastResponse.code() == HttpURLConnection.HTTP_MOVED_PERM || lastResponse.code() == HttpURLConnection.HTTP_MOVED_TEMP)){
					if(lastResponse.header("Location").contains("login")){
						isLogin = false;
						errorCallback.onNeedLogin();
					}
				}else{
					ResponseBody body = response.body();
					callback.onSuccess(response, body == null ? null : body.string());
				}
			}
		});
	}
	
	
	/**
	 * 异步检查登陆状态，有账号信息时自动进行登录
	 * @see #loginAsync(RequestCallback, RequestErrorCallback)
	 */
	public abstract void checkLoginAsync(RequestCallback callback, RequestErrorCallback errorCallback);
	
	/**
	 * 异步登录请求 (使用储存的用户名和密码)
	 * @see #loginAsync(String, String, RequestCallback, RequestErrorCallback)
	 */
	public void loginAsync(RequestCallback callback, RequestErrorCallback errorCallback){
		String userName = SettingUtil.getString(accountName, null);
		if(userName == null){
			errorCallback.onNeedLogin();
			return;
		}
		String password = SettingUtil.getString(passwordName, null);
		if(password == null){
			errorCallback.onNeedLogin();
			return;
		}
		loginAsync(userName, password, callback, errorCallback);
	}
	
	/**
	 * 异步登录请求
	 * @param callback 登录成功时的回调
	 * @param errorCallback 网络错误或者用户名或密码错误时的回调
	 */
	public abstract void loginAsync(String userName, String password, RequestCallback callback, RequestErrorCallback errorCallback);
	
	
	/**
	 * 同步GET请求
	 */
	public Response get(String url) throws IOException, NeedLoginException{
		if(isLogin || checkLoginSync()){
			return okHttpClient.newCall(new Request.Builder().url(host + url).build()).execute();
		}else{
			throw new NeedLoginException();
		}
	}
	
	/**
	 * 同步POST请求
	 */
	public Response post(String url, RequestBody body) throws IOException, NeedLoginException{
		if(isLogin || checkLoginSync()){
			return okHttpClient.newCall(new Request.Builder().url(host + url).post(body).build()).execute();
		}else{
			throw new NeedLoginException();
		}
	}
	
	/**
	 * 同步GET请求，不检查登录状态
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Response getWithOutCheck(String url) throws IOException{
		return okHttpClient.newCall(new Request.Builder().url(host + url).build()).execute();
	}
	
	/**
	 * 同步POST请求，不检查登录状态
	 * @param url
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	public Response postWithOutCheck(String url, RequestBody requestBody) throws IOException{
		return okHttpClient.newCall(new Request.Builder().url(host + url).post(requestBody).build()).execute();
	}
	
	/**
	 * 同步检查登陆状态，有账号信息时自动进行登录
	 * @see #loginSync()
	 */
	public abstract boolean checkLoginSync() throws IOException;
	
	/**
	 * 同步登录请求 (使用储存的用户名和密码)
	 * @see #loginSync(String, String)
	 */
	public synchronized boolean loginSync() throws IOException{
		String userName = SettingUtil.getString(accountName, null);
		if(userName == null) return false;
		String password = SettingUtil.getString(passwordName, null);
		if(password == null) return false;
		return loginSync(userName, password);
	}
	
	/**
	 * 同步登录请求
	 * @return 登录是否成功（失败的情况下为账号或密码错误）
	 * @throws IOException 网络错误
	 */
	public abstract boolean loginSync(String userName, String password) throws IOException;
	
	/**
	 * 获取用户名
	 * @return
	 */
	public String getAccountName(){
		return accountName;
	}
}
