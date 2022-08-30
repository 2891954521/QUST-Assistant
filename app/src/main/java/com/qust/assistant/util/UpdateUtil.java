package com.qust.assistant.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.qust.assistant.App;
import com.qust.assistant.ui.app.UpdateActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateUtil{
	
	public static final String NAVIGATION_PAGE_URL = "http://139.224.16.208/guide.json";
	
	public static void checkUpdate(final Activity activity){
		
		if(!(boolean)SettingUtil.get(SettingUtil.KEY_AUTO_UPDATE, true)) return;
		
		boolean isDev = (boolean)SettingUtil.get(SettingUtil.KEY_UPDATE_DEV, false);
		
		long frequency = isDev ? 1000 * 60 * 60 * 24 * 1 : 1000 * 60 * 60 * 24 * 3;
		
		long current = System.currentTimeMillis();
		
		if(current - (long)SettingUtil.get(SettingUtil.LAST_UPDATE_TIME, 0L) < frequency) return;
		
		SettingUtil.edit().putLong("last_update_time", current).apply();
		
		new AsyncTask<Void, Void, JSONObject>(){
			
			@Override
			protected JSONObject doInBackground(Void... voids){
				return checkVersion(activity, isDev);
			}
			
			@Override
			protected void onPostExecute(JSONObject data){
				if(data == null) return;
				try{
					DialogUtil.getBaseDialog(activity).title("更新")
							.content("检查到新版本，是否更新？\n" + data.getString("message"))
							.onPositive((dialog, which) -> activity.startActivity(new Intent(activity, UpdateActivity.class)))
							.show();
				}catch(JSONException ignored){ }
			}
		}.execute();
	}
	
	/**
	 * 检查更新
	 * @param isDev 是否检查开发版更新
	 */
	@Nullable
	public static JSONObject checkVersion(Context context, boolean isDev){
		try{
			String response = WebUtil.doGet(NAVIGATION_PAGE_URL, null);
			JSONObject json;
			if(response.length() > 0){
				json = new JSONObject(response);
			}else{
				return null;
			}
			
			if(isDev){
				JSONObject data = checkVersion(App.DEV_VERSION, json.getString("getDevInfo"));
				if(data != null) return data;
			}
			
			int versionCode;
			
			try{
				PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				versionCode = pkg.versionCode;
			}catch(PackageManager.NameNotFoundException ignored){
				return null;
			}
			
			return checkVersion(versionCode, json.getString("getUpdateInfo"));
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			return null;
		}
	}
	
	/**
	 * 检查版本更新
	 * @return 版本信息，没有新版本时返还null
	 */
	@Nullable
	public static JSONObject checkVersion(int version, String url) throws JSONException, IOException{
		String response = WebUtil.doGet(url, null);
		if(response.length() == 0){
			return null;
		}
		
		JSONObject json = new JSONObject(response);
		if(json.has("code") && json.getInt("code") == 200){
			JSONObject data = json.getJSONObject("data");
			return data.getInt("version") > version ? data : null;
		}else{
			return null;
		}
	}
	
}
