package com.university.assistant.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.ui.UpdateActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateUtil{
	
	public static void checkUpdate(Activity activity){
		try{
			JSONObject js = getUpdateInfo();
			if(js.getInt("code") == 200){
				JSONObject data = js.getJSONObject("data");
				if(checkVersion(activity,data.getInt("version"))){
					activity.runOnUiThread(new Thread(){
						@Override
						public void run(){
							try{
								DialogUtil.getBaseDialog(activity).title("更新")
										.content("检查到新版本，是否更新？\n" + data.getString("message"))
										.onPositive((dialog,which) -> activity.startActivity(new Intent(activity,UpdateActivity.class))).show();
							}catch(JSONException e){
								LogUtil.Log(e);
							}
						}
					});
				}
			}
		}catch(JSONException e){
			LogUtil.Log(e);
		}
	}
	
	public static JSONObject getUpdateInfo(){
		try{
			String response = WebUtil.doGet("http://139.224.16.208/cloud/apk/getUpdateInfo.php",null);
			if(response != null){
				return new JSONObject(response);
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return new JSONObject();
	}
	
	
	public static boolean checkVersion(Context context,int newVersion){
		int versionCode = -1;
		
		try {
			PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionCode = pkg.versionCode;
		} catch (PackageManager.NameNotFoundException ignored) { }
		
		return versionCode != -1 && newVersion > versionCode;
	}
}
