package com.qust.assistant.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.qust.assistant.App;
import com.qust.assistant.ui.UpdateActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateUtil{
	
	public static void checkUpdate(final Activity activity){
		
		SharedPreferences setting = SettingUtil.setting;
		
		if(!setting.getBoolean("key_auto_update", true)) return;
		
		boolean isDev = setting.getBoolean("key_update_dev", false);
		
		long frequency = isDev ? 1000 * 60 * 60 * 24 * 3 : 1000 * 60 * 60 * 24 * 7;
		
		long current = System.currentTimeMillis();
		
		if(current - setting.getLong("last_update_time", 0L) < frequency) return;
		
		setting.edit().putLong("last_update_time", current).apply();
		
		new AsyncTask<Void, Void, JSONObject>(){
			
			@Override
			protected JSONObject doInBackground(Void... voids){
				try{
					JSONObject json = UpdateUtil.getUpdateInfo(isDev);
					JSONObject data = json.getJSONObject("data");
					if(json.getInt("code") == 200){
						if(UpdateUtil.checkVersion(activity, data.getInt("version"), isDev)) return data;
					}
				}catch(JSONException ignored){ }
				return null;
			}
			
			@Override
			protected void onPostExecute(JSONObject json){
				if(json == null) return;
				try{
					DialogUtil.getBaseDialog(activity).title("更新")
							.content("检查到新版本，是否更新？\n" + json.getString("message"))
							.onPositive((dialog, which) -> activity.startActivity(new Intent(activity, UpdateActivity.class))).show();
				}catch(JSONException ignored){ }
			}
		}.execute();
	}
	
	public static JSONObject getUpdateInfo(boolean isDev){
		try{
			String response = WebUtil.doGet("http://139.224.16.208/guide.json", null);
			String url = "http://139.224.16.208/cloud/apk/getUpdateInfo.php";
			
			if(response != null){
				JSONObject js = new JSONObject(response);
				if(isDev){
					if(js.has("getDevInfo")) url = js.getString("getDevInfo");
				}else{
					if(js.has("getUpdateInfo")) url = js.getString("getUpdateInfo");
				}
			}
			
			response = WebUtil.doGet(url, null);
			if(response != null){
				return new JSONObject(response);
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return new JSONObject();
	}
	
	public static boolean checkVersion(Context context, int newVersion, boolean isDev){
		
		if(isDev){
			return App.DEV_VERSION < newVersion;
		}
		
		int versionCode = -1;
		
		try{
			PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionCode = pkg.versionCode;
		}catch(PackageManager.NameNotFoundException ignored){
		}
		
		return versionCode != -1 && newVersion > versionCode;
	}
}
