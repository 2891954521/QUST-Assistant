package com.qust.assistant.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class LogUtil{
	
	// Log路径
	public static String LogFile;
	
	public static boolean DEBUG;
	
	public static void init(Context context){
		
		DEBUG = SettingUtil.getBoolean("key_debug", false);
		
		File f = context.getExternalFilesDir("log");
		if(!f.exists()) f.mkdirs();
		
		LogFile = f.toString();
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
	public static void Log(Throwable e){
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		String str = stringWriter.toString();
		int hash = str.hashCode();
		File f = new File(LogFile, hash + ".log");
		if(f.exists()){
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + str.hashCode());
		}else{
			FileUtil.writeFile(f.toString(), str);
			FileUtil.appendFile(new File(LogFile, "upload.log"), hash + "\n");
			debugLog(DateUtil.YMD_HM.format(new Date(System.currentTimeMillis())) + " 发生异常:" + str.hashCode() + "\n" + e.getClass().getName() + " : " + e.getMessage());
		}
	}
}
