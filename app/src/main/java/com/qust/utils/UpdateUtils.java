package com.qust.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.app.UpdateActivity;
import com.qust.assistant.App;
import com.qust.assistant.BuildConfig;
import com.qust.assistant.R;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.UpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * 检查更新的工具类
 */
public class UpdateUtils{
	
	public static final String NAVIGATION_PAGE_URL = "https://nacho.kituin.fun/guide.json";
	
	public static final String GITHUB_UPDATE_URL = "https://api.github.com/repos/2891954521/QUST-Assistant/releases/latest";
	
	public static final String GITEE_UPDATE_URL = "https://gitee.com/api/v5/repos/berdb/QUST-Assistant/releases/latest";
	
	/**
	 * 异步检查更新
	 */
	public static void checkUpdateAsync(@NonNull final Activity activity){
		
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
				UpdateInfo info;
				
				// 从 Gitee 上检查更新
				info = checkVersionFromGit(GITEE_UPDATE_URL);
			
				// 从 Github 上检查更新
				if(info == null) info = checkVersionFromGit(GITHUB_UPDATE_URL);
				
				// 从私有源检查更新
				if(info == null) checkVersion(activity, isDev);
				
				if(info != null){
					UpdateInfo finalInfo = info;
					activity.runOnUiThread(() -> DialogUtil.getBaseDialog(activity)
							.title("更新")
							.canceledOnTouchOutside(false)
							.content("检查到新版本，是否更新？\n" + finalInfo.message)
							.onPositive((dialog, which) -> activity.startActivity(new Intent(activity, UpdateActivity.class)))
							.show());
				}
			}
		}.start();
	}
	
	/**
	 * 检查更新
	 * @param isDev 是否检查开发版更新
	 */
	@Nullable
	public static UpdateInfo checkVersion(Context context, boolean isDev){
		try{
			String response = WebUtil.doGet(NAVIGATION_PAGE_URL, null);
			if(TextUtils.isEmpty(response)) return null;
			
			JSONObject json = new JSONObject(response);

			if(isDev){
				// 检查开发版更新
				UpdateInfo info = checkVersion(App.DEV_VERSION, json.getString("getDevInfo"));
				if(info != null){
					info.isDev = true;
					return info;
				}
			}else{
				PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				return checkVersion(pkg.versionCode, json.getString("getUpdateInfo"));
			}
		}catch(IOException | PackageManager.NameNotFoundException ignore){
		
		}catch(JSONException e){
			LogUtil.Log(e);
		}
		return null;
	}
	
	/**
	 * 从Git检查版本更新
	 * @return 版本信息，没有新版本时返还 null
	 */
	@Nullable
	public static UpdateInfo checkVersionFromGit(String url){
		try{
			String response = WebUtil.doGet(url, null);
			if(response.length() == 0) return null;
			
			JSONObject json = new JSONObject(response);
			Date buildDate = DateUtil.YMD.parse(BuildConfig.PACKAGE_TIME);
			Date publishDate = DateUtil.YMD.parse(json.getString("created_at"));
			
			if(publishDate == null) return null;

			if(publishDate.after(buildDate)){
				JSONArray assets = json.getJSONArray("assets");
				if(assets.length() == 0) return null;
				
				UpdateInfo info = new UpdateInfo();
				info.versionName = json.getString("tag_name");
				info.message = json.getString("body");
				info.apkUrl = assets.getJSONObject(0).getString("browser_download_url");
				return info;
			}
		}catch(IOException | ParseException ignore){
		
		}catch(JSONException e){
			LogUtil.Log(e);
		}
		return null;
	}
	
	/**
	 * 从私有源检查版本更新
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
			System.out.println(data);
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
