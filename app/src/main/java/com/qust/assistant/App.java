package com.qust.assistant;

import android.app.Application;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.billy.android.swipe.SmartSwipeBack;
import com.billy.android.swipe.SwipeConsumer;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.app.GuideActivity;
import com.qust.assistant.ui.fragment.third.DrinkViewModel;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

public class App extends Application{
	
	/**
	 * 开发版版本号
	 */
	public static final int DEV_VERSION = 21;
	
	/*
	 * Handler 公用的 what 值
	 */
	/**
	 * 更新 Dialog
	 */
	public static final int UPDATE_DIALOG = 0;
	
	/**
	 * 关闭 Dialog 并 Toast
	 */
	public static final int DISMISS_TOAST = 1;
	
	/**
	 * 更新 AdapterView
	 */
	public static final int NOTIFY_TOAST = 2;
	
	/**
	 * 仅 Toast
	 */
	public static final int TOAST = 3;
	
	public LoginViewModel loginViewModel;
	
	public DrinkViewModel drinkViewModel;
	
	public LessonTableViewModel lessonTableViewModel;
	
	private Thread.UncaughtExceptionHandler handler;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		SettingUtil.init(this);
		
		LogUtil.init(this);
		
		loginViewModel = new LoginViewModel(this);
		drinkViewModel = new DrinkViewModel(this);
		lessonTableViewModel = new LessonTableViewModel(this);
		
		if(SettingUtil.getBoolean(getString(R.string.KEY_THEME_DARK), false)){
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
		}else if(!SettingUtil.getBoolean(getString(R.string.KEY_THEME_FOLLOW_SYSTEM), true)){
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		}
		
		handler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		// 初始化滑动返回框架
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			SmartSwipeBack.activityBezierBack(this, activitySwipeBackFilter, 0);
		} else {
			SmartSwipeBack.activitySlidingBack(this, activitySwipeBackFilter, 0, 0x80000000, 0, 0, 0.5f, SwipeConsumer.DIRECTION_LEFT);
		}
	}
	
	public void toast(final String message){
		new Thread(){
			@Override
			public void run(){
				Looper.prepare();
				Toast.makeText(App.this, message, Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
	}
	
	// 捕获异常
	private class ExceptionHandler implements Thread.UncaughtExceptionHandler{
		@Override
		public void uncaughtException(@NonNull Thread thread, @NonNull final Throwable throwable){
			toast("应用发生错误，错误类型：" + throwable.getClass());
			LogUtil.Log("-------应用异常退出-------", throwable);
			LogUtil.debugLog("-------应用异常退出-------\n");
			if(handler != null) handler.uncaughtException(thread, throwable);
		}
	}
	
	private final SmartSwipeBack.ActivitySwipeBackFilter activitySwipeBackFilter = activity -> !(activity instanceof MainActivity || activity instanceof GuideActivity);
}
