package com.qust.assistant.util;

import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.App;
import com.qust.assistant.BuildConfig;
import com.qust.assistant.R;

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
	
	private static boolean UPLOAD_LOG;
	
	/**
	 * 初始化日志模块并处理日志上报
	 */
	public static void init(@NonNull App app){
		
		DEBUG = SettingUtil.getBoolean("key_debug", false);
		
		File f = app.getExternalFilesDir("log");
		if(!f.exists()) f.mkdirs();
		
		LogFile = f.toString();
		
		UPLOAD_LOG = SettingUtil.getBoolean(app.getString(R.string.KEY_UPLOAD_LOG), true);
		
		if(UPLOAD_LOG){
			File uploadData = new File(LogUtil.LogFile, "upload.log");
			if(uploadData.exists()){
				new LogThread(app).start();
			}
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

	/**
	 * 强制输出到 debug 文件
 	 */
	public static void debugLog(String string){
		FileUtil.appendFile(new File(LogFile, "debug.log"), string + "\n");
	}
	
	/**
	 * Log 输出异常
	 */
	public static void Log(@NonNull Throwable e){
		Log(e, UPLOAD_LOG);
	}
	
	/**
	 * Log 输出异常
	 */
	public static void Log(@Nullable String s, @NonNull Throwable e){
		Log(s, e, UPLOAD_LOG);
	}
	
	/**
	 * Log 输出异常
	 * @param e 异常
	 * @param needUpload 是否需要上报
	 */
	public static void Log(@NonNull Throwable e, boolean needUpload){
		Log(null, e, needUpload);
	}
	
	/**
	 * Log 输出异常
	 * @param msg 附加错误信息
	 * @param e 异常
	 * @param needUpload 是否需要上报
	 */
	public static void Log(@Nullable String msg, @NonNull Throwable e, boolean needUpload){
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		
		String str = stringWriter.toString();
		
		if(msg != null) str += msg.length() > 4096 ? msg.substring(0, 4096) : msg;
		
		int hash = str.hashCode();
		File f = new File(LogFile, hash + ".log");
		
		if(f.exists()){
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + hash);
			if(needUpload){
				try{
					FileUtil.appendFileNewLine(new File(LogFile, "upload.log"), new JSONObject().put("hash", hash).toString());
				}catch(JSONException ignored){ }
			}
		}else{
			FileUtil.writeFile(f.toString(), str);
			String message = e.getClass().getName() + " : " + e.getMessage();
			if(needUpload){
				try{
					FileUtil.appendFileNewLine(new File(LogFile, "upload.log"), new JSONObject().put("hash", hash).put("message", message).toString());
				}catch(JSONException ignored){ }
			}
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + hash + "\n" + message);
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
			
			JSONObject js;
			for(String log : logs){
				try{
					js = new JSONObject(log);
					String hash = js.getString("hash");
					String name = js.has("message") ? js.getString("message") : "";
					String content = js.has("message") ? URLEncoder.encode(FileUtil.readFile(new File(LogUtil.LogFile, hash + ".log")), "UTF-8") : null;
					
					String data = "id=" + ANDROID_ID +	// 设备ID
							"&hash=" + hash +			// 异常hash
							"&name=" + name +			// 异常简述
							"&brand=" + Build.BRAND +	// 手机品牌
							"&model=" + Build.MODEL +	// 手机型号
							"&android=" + Build.VERSION.RELEASE + // 系统版本号
							"&appver=" + version + 		// app版本号
							"&devVer=" + devVer;		// 开发版版本号
					
					if(content != null) data += "&content=" + content;
					
					String result = WebUtil.doPost(UPLOAD_LOG_URL,null, data, "ContentType", "multipart/form-data");
					
					LogUtil.Log(result);
					
					js = new JSONObject(result);
					
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
