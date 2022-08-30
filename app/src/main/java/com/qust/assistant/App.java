package com.qust.assistant;

import android.app.Application;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.billy.android.swipe.SmartSwipeBack;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.sql.DataBase;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

public class App extends Application{
	
	public static final String APP_USER_LOGIN = "user_login";
	
	public static final int APP_REQUEST_CODE = 10;
	
	/**
	 * 开发版版本号
	 */
	public static final int DEV_VERSION = 11;
	
	/**
	 * Handler 公用的 what 值
	 */
	// 关闭 Dialog 并 Toast
	public static final int DISMISS_TOAST = 1;
	//
	public static final int NOTIFY_TOAST = 2;
	// 更新 Dialog
	public static final int UPDATE_DIALOG = 0;
	
	private Thread.UncaughtExceptionHandler handler;
	
	public LessonTableViewModel lessonTableViewModel;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		SettingUtil.init(this);
		
		DataBase.init(this);
		
		LogUtil.init(this);
		
		lessonTableViewModel = new LessonTableViewModel(this);
		
		handler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			// use bezier back before LOLLIPOP
//			SmartSwipeBack.activityBezierBack(this, activitySwipeBackFilter, 0);
//		} else {
//			// add relative moving slide back
//			SmartSwipeBack.activitySlidingBack(this, activitySwipeBackFilter, 0, 0x80000000, 0, 0, 0.5f, DIRECTION_LEFT);
//		}
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
			LogUtil.Log("-------应用发生异常-------", throwable);
			LogUtil.debugLog("-------应用异常退出-------");
			if(handler != null) handler.uncaughtException(thread, throwable);
		}
	}
	
	private final SmartSwipeBack.ActivitySwipeBackFilter activitySwipeBackFilter = activity -> !(activity instanceof MainActivity);
}
