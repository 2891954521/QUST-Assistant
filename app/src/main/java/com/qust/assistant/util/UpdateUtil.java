package com.qust.assistant.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.qust.assistant.App;
import com.qust.assistant.BuildConfig;
import com.qust.assistant.R;
import com.qust.app.UpdateActivity;
import com.qust.assistant.vo.UpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class UpdateUtil{
	
	public static final String NAVIGATION_PAGE_URL = "http://139.224.16.208/guide.json";
	
	public static final String GITHUB_UPDATE_URL = "https://api.github.com/repos/2891954521/QUST-Assistant/releases/latest";
	
	/**
	 * 异步检查更新
	 */
	public static void checkUpdate(final Activity activity){
		
		if(!SettingUtil.getBoolean(activity.getString(R.string.KEY_AUTO_UPDATE), true)){
			return;
		}
		
		boolean isDev = SettingUtil.getBoolean(activity.getString(R.string.KEY_UPDATE_DEV), false);
		
		long current = System.currentTimeMillis();
		long frequency = isDev ? 1000 * 60 * 60 * 24 * 1 : 1000 * 60 * 60 * 24 * 3;
		
		if(current - (long)SettingUtil.get(activity.getString(R.string.last_update_time), 0L) < frequency){
			return;
		}
		
		SettingUtil.edit().putLong(activity.getString(R.string.last_update_time), current).apply();
		
		new Thread(){
			@Override
			public void run(){
				UpdateInfo info = checkVersion(activity, isDev, false);
				if(info != null){
					activity.runOnUiThread(() -> {
						DialogUtil.getBaseDialog(activity).title("更新").content("检查到新版本，是否更新？\n" + info.message)
								.onPositive((dialog, which) -> activity.startActivity(new Intent(activity, UpdateActivity.class)))
								.show();
					});
				}
			}
		}.start();
	}
	
	/**
	 * 检查更新
	 * @param isDev 是否检查开发版更新
	 * @param isSpare 是否使用备用更新连接
	 */
	@Nullable
	public static UpdateInfo checkVersion(Context context, boolean isDev, boolean isSpare){
		if(!isSpare){
			// 从 Github 上检查更新
			try{
				UpdateInfo info = checkVersionFromGitHub();
				if(info != null) return info;
			}catch(IOException | JSONException ignore){ }
		}
		
		try{
			String response = WebUtil.doGet(NAVIGATION_PAGE_URL, null);
			JSONObject json;
			if(response.length() > 0){
				json = new JSONObject(response);
			}else{
				return null;
			}
			
			// 检查开发版更新
			if(isDev){
				try{
					UpdateInfo info = checkVersion(App.DEV_VERSION, json.getString("getDevInfo"));
					if(info != null){
						info.isDev = true;
						return info;
					}
				}catch(IOException | JSONException e){
					LogUtil.Log(e);
				}
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
			LogUtil.Log(e, false);
			return null;
		}
	}
	
	
	/**
	 * 从Github检查版本更新
	 * @return 版本信息，没有新版本时返还 null
	 */
	@Nullable
	public static UpdateInfo checkVersionFromGitHub() throws JSONException, IOException{
		String response = WebUtil.doGet(GITHUB_UPDATE_URL, null);
		if(response.length() == 0){
			return null;
		}
		
		JSONObject json = new JSONObject(response);
		Date buildDate;
		Date publishDate;
		try{
			buildDate = DateUtil.YMD.parse(BuildConfig.PACKAGE_TIME);
			publishDate = DateUtil.YMD.parse(json.getString("published_at"));
			if(publishDate == null) return null;
		}catch(ParseException e){
			e.printStackTrace();
			return null;
		}
		if(publishDate.after(buildDate)){
			JSONArray assets = json.getJSONArray("assets");
			if(assets.length() == 0){
				return null;
			}
			
			UpdateInfo info = new UpdateInfo();
			info.versionName = json.getString("tag_name");
			info.message = json.getString("body");
			info.apkUrl = assets.getJSONObject(0).getString("browser_download_url");
			return info;
		}
		
		return null;
	}

	
	/**
	 * 检查版本更新
	 * @return 版本信息，没有新版本时返还null
	 */
	@Nullable
	private static UpdateInfo checkVersion(int versionCode, String url) throws JSONException, IOException{
		String response = WebUtil.doGet(url, null);
		if(response.length() == 0){
			return null;
		}
		
		JSONObject json = new JSONObject(response);
		if(json.has("code") && json.getInt("code") == 200){
			JSONObject data = json.getJSONObject("data");
			if(data.getInt("versionCode") > versionCode){
				UpdateInfo info = new UpdateInfo();
				info.versionCode = data.getInt("versionCode");
				info.versionName = data.getString("versionName");
				info.message = data.getString("message");
				info.apkUrl = data.getString("apkUrl");
				return info;
			}
		}
		
		return null;
	}
	
}
