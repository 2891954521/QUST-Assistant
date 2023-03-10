package com.qust.assistant.util;

import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qust.assistant.App;
import com.qust.assistant.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Date;

public class LogUtil{
	
	private static final String UPLOAD_LOG_URL = "http://139.224.16.208/cloud/log/uploadLog.php";
	// Log路径
	public static String LogFile;
	
	private static boolean DEBUG;
	
	/**
	 * 初始化日志模块并处理日志上报
	 */
	public static void init(@NonNull App app){
		
		DEBUG = SettingUtil.getBoolean("key_debug", false);
		
		File f = app.getExternalFilesDir("log");
		if(!f.exists()) f.mkdirs();
		
		LogFile = f.toString();
		
		File uploadData = new File(LogUtil.LogFile, "upload.log");
		if(uploadData.exists()){
			new LogThread(app).start();
		}
	}
	
	/**
	 * 调试模式下才会进行输出
	 * @param msg message
	 */
	public static void Log(String msg){
		if(DEBUG){
			Log.i("i", msg);
			debugLog(msg);
		}
	}
	
	// 输出log
	public static void Log(String s, Throwable e){
		debugLog(s);
		Log(e);
	}
	
	/**
	 * 强制输出到 debug 文件
 	 */
	public static void debugLog(String string){
		FileUtil.appendFile(new File(LogFile, "debug.log"), string + "\n");
	}
	
	// log输出异常
	public static void Log(@NonNull Throwable e){
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		String str = stringWriter.toString();
		int hash = str.hashCode();
		File f = new File(LogFile, hash + ".log");
		if(f.exists()){
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + str.hashCode());
			FileUtil.appendFile(new File(LogFile, "upload.log"), hash + "\n");
		}else{
			FileUtil.writeFile(f.toString(), str);
			String message = e.getClass().getName() + " : " + e.getMessage();
			FileUtil.appendFile(new File(LogFile, "upload.log"), hash + "|" + message + "\n");
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + str.hashCode() + "\n" + message);
		}
	}
	
	/**
	 * 日志上报线程
	 */
	public static class LogThread extends Thread{
		
		private App context;
		
		public LogThread(App context){
			this.context = context;
		}
		
		@Override
		public void run(){
			
			File f = new File(LogUtil.LogFile, "upload.log");
			
			String version = BuildConfig.VERSION_NAME;
			String devVer = String.valueOf(App.DEV_VERSION);
			String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
			
			String[] logs = FileUtil.readFile(f).split("\n");
			
			f.delete();
			
			for(String log : logs){
				try{
					int index = log.indexOf('|');
					String hash = index == -1 ? log : log.substring(0, index);
					String name = (index == -1 || index + 1 == log.length()) ? null : log.substring(index + 1);
					String content = index == -1 ? null : URLEncoder.encode(FileUtil.readFile(new File(LogUtil.LogFile, hash + ".log")), "UTF-8");
					
					String result = WebUtil.doPost(UPLOAD_LOG_URL,null,
							"id=" + ANDROID_ID +		// 设备ID
								"&hash=" + hash +			// 异常hash
								"&name=" + name +			// 异常简述
								"&brand=" + Build.BRAND +	// 手机品牌
								"&model=" + Build.MODEL +	// 手机型号
								"&android=" + Build.VERSION.RELEASE + // 系统版本号
								"&appver=" + version + 		// app版本号
								"&devVer=" + devVer + 		// 开发版版本号
								(content == null ? "" : "&content=" + content),
							"ContentType", "multipart/form-data");
					
					LogUtil.debugLog(result);
					
					JSONObject js = new JSONObject(result);
					
					if(js.getInt("code") != 200){
						LogUtil.debugLog("日志上传失败：" + js.getString("msg"));
					}
					
				}catch(IOException | JSONException | IndexOutOfBoundsException e){
					LogUtil.debugLog("日志上传失败：" + e.getMessage());
				}
			}
		}
	}
}
