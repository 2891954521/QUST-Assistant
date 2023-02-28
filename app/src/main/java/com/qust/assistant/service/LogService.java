package com.qust.assistant.service;

import android.os.Build;
import android.provider.Settings;

import com.qust.assistant.App;
import com.qust.assistant.BuildConfig;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 日志上报Service
 */
public class LogService extends Thread{
	
	private App context;
	
	public LogService(App context){
		this.context = context;
	}
	
	@Override
	public void run(){
		
		File f = new File(LogUtil.LogFile, "upload.log");
		
		if(!f.exists()){
			return;
		}
		
		String version = BuildConfig.VERSION_NAME;
		String devVer = String.valueOf(App.DEV_VERSION);
		String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		
		String[] logs = FileUtil.readFile(f).split("\n");
		
		f.delete();
		
		for(String log : logs){
			try{
				String str = URLEncoder.encode(FileUtil.readFile(new File(LogUtil.LogFile, log + ".log")), "UTF-8");
				
				String result = WebUtil.doPost("http://139.224.16.208/cloud/log/uploadLog.php",null,
						"id=" + ANDROID_ID +	// 设备ID
						"&hash=" + log +			// 异常hash
						"&brand=" + Build.BRAND +	// 手机品牌
						"&model=" + Build.MODEL +	// 手机型号
						"&android=" + Build.VERSION.RELEASE + // 系统版本号
						"&appver=" + version + 		// app版本号
						"&devVer=" + devVer + 		// 开发版版本号
						"&content=" + str,
						"ContentType", "multipart/form-data");
				
				JSONObject js = new JSONObject(result);
				
				if(js.getInt("code") != 200){
					LogUtil.debugLog("日志上传失败：" + js.getString("msg"));
				}
				
			}catch(IOException | JSONException e){
				LogUtil.debugLog("日志上传失败：" + e.getMessage());
			}
		}
	}
}