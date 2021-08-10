package com.university.assistant;

import android.app.Application;
import android.os.Looper;
import android.widget.Toast;

import com.university.assistant.util.LogUtil;

import androidx.annotation.NonNull;

public class App extends Application{
	
	public static final String APP_UPDATE_LESSON_TABLE = "update.lesson.table";
	
	public static final int APP_REQUEST_CODE = 10;
	
	public static final String CREATE_NOTE_TABLE =
			"CREATE TABLE `Note` (" +
					"`id` INTEGER primary key autoincrement, " +
					"`isFinish` BOOLEAN," + // 是否超过截止日期
					"`title` TINYTEXT," + // 最长255
					"`text` TEXT," + // 最长65535
					"`items` TEXT," +
					"`date` DATETIME," +
					"`deadline` DATETIME," +
					"`pictures` TEXT" +
					");";
	
	public static final String CREATE_PICTURE_TABLE =
			"CREATE TABLE `Picture` (" +
					"`id` INTEGER primary key autoincrement, " +
					"`file` TEXT," +
					"`date` DATETIME" +
					");";
	
	public static final int DEV_VERSION = 0;
	
	private Thread.UncaughtExceptionHandler handler;
	
	@Override
	public void onCreate(){
		super.onCreate();
		LogUtil.init(this);
		handler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
	}
	
	public void toast(String message){
		new Thread(){
			@Override
			public void run(){
				Looper.prepare();
				Toast.makeText(App.this,message,Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
	}
	
	// 捕获异常
	private class ExceptionHandler implements Thread.UncaughtExceptionHandler{
		@Override
		public void uncaughtException(@NonNull Thread thread,@NonNull final Throwable throwable){
			new Thread(){
				@Override
				public void run(){
					Looper.prepare();
					Toast.makeText(App.this,"应用发生错误，错误类型：" + throwable.getClass().toString(),Toast.LENGTH_LONG).show();
					Looper.loop();
				}
			}.start();
			LogUtil.Log("-------应用发生异常-------",throwable);
			LogUtil.debugLog("-------应用异常退出-------");
			if(handler!=null)handler.uncaughtException(thread,throwable);
		}
	}
	
}
