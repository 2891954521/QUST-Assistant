package com.qust.assistant;

import android.app.Application;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.billy.android.swipe.SmartSwipeBack;
import com.billy.android.swipe.SwipeConsumer;
import com.qust.app.GuideActivity;
import com.qust.app.MainActivity;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.fragment.third.DrinkViewModel;

public class App extends Application{
	
	/**
	 * 开发版版本号
	 */
	public static final int DEV_VERSION = 25;
	
	
	public DrinkViewModel drinkViewModel;
	
	private Thread.UncaughtExceptionHandler handler;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		SettingUtil.init(this);
		
		LogUtil.init(this);
		
		drinkViewModel = new DrinkViewModel(this);
		
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
