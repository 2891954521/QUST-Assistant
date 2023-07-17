package com.qust.assistant.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.base.QFragmentActivity;
import com.qust.assistant.ui.fragment.school.LoginFragment;
import com.qust.assistant.util.QustUtil.LoginUtil;
import com.qust.assistant.util.QustUtil.QustAPI;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.QustLoginResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

public class LoginViewModel extends AndroidViewModel{
	
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
		
		host = QustAPI.SEVER_HOSTS[0];
		
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
				loginResult.postValue(loginResult.getValue());
				return;
			}
		}catch(IOException e){
			loginResult.postValue(new QustLoginResult(handler, "登录失败" + e.getMessage(), null));
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
		QustLoginResult result = LoginUtil.login(name, password, host, new LoginUtil.LoginProgress(){
			@Override
			public void onProgress(String msg){
				handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, msg));
			}
		});
		result.from = handler;
		
		if(result.cookie != null){
			SettingUtil.edit()
					.putString(getApplication().getString(R.string.SCHOOL_NAME), name)
					.putString(getApplication().getString(R.string.SCHOOL_PASSWORD), password).apply();
		}
		
		loginResult.postValue(result);
	}
	
	/**
	 * 同步登录请求，使用储存的用户名和密码，返还Cookie，会更新ViewModel里的Cookie
	 * @return Cookie
	 */
	public synchronized String login(){
		try{
			if(checkLogin()) return getCookie();
		}catch(IOException ignored){ }
		
		if(name == null || password == null){
			return null;
		}else{
			QustLoginResult result = LoginUtil.login(name, password, host);
			loginResult.postValue(result);
			return result.cookie;
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
	
}
