package com.qust.assistant.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.UpdateUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

public class UpdateActivity extends BaseAnimActivity{
	
	private MaterialDialog checkDialog;
	
	private MaterialDialog downloadDialog;
	
	private File file;
	
	private String message;
	
	private String md5;
	
	private String url;
	
	private App app;
	
	private int version;
	
	private boolean hasCheck;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		
		initStatusBar();
		
		app = (App)getApplication();
		
		file = new File(getExternalCacheDir(),"release.apk");
		
		findViewById(R.id.activity_update_button).setOnClickListener(v -> {
			if(url == null){
				checkUpdate();
			}else{
//				if(file.exists()){
//					checkPackage();
//				}else{
					downloadApk();
//				}
			}
		});
		
		checkDialog = new MaterialDialog.Builder(this)
				.title("正在检查更新")
				.progress(true,0)
				.build();
		
		checkDialog.setCanceledOnTouchOutside(false);
		
		downloadDialog = new MaterialDialog.Builder(this)
				.title("正在下载")
				.progress(true,0)
				.build();
		
		downloadDialog.setCanceledOnTouchOutside(false);
		
		findViewById(R.id.toolbar).postDelayed(this::checkUpdate,200);
		
		try{
			PackageInfo pkg = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.activity_update_current_version)).setText("当前版本：" + pkg.versionName);
		}catch(PackageManager.NameNotFoundException ignore){ }
		
		initToolBar(null);
		
	}
	
	private void checkUpdate(){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isDev = setting.getBoolean("key_update_dev",false);
		checkDialog.show();
		new Thread(){
			@Override
			public void run(){
				JSONObject js = UpdateUtil.getUpdateInfo(isDev);
				try{
					if(js.has("code")){
						if(js.getInt("code")==200){
							JSONObject json = js.getJSONObject("data");
							version = json.getInt("version");
							if(UpdateUtil.checkVersion(UpdateActivity.this, version, isDev)){
								message = json.getString("message");
								md5 = json.getString("md5");
								url = json.getString("apkUrl");
								runOnUiThread(() -> ((TextView)findViewById(R.id.activity_update_info)).setText("下载地址：" + url + "\n" + message));
							}else{
								app.toast("当前无新版本！");
							}
						}else{
							app.toast(js.getString("msg"));
						}
					}else{
						app.toast("获取更新信息失败！");
					}
				}catch(JSONException e){
					LogUtil.Log(e);
					app.toast("获取更新信息失败！");
				}
				if(checkDialog.isShowing())runOnUiThread(() -> checkDialog.cancel());
			}
		}.start();
	}
	
	private void downloadApk(){
		downloadDialog.show();
		new Thread(){
			@Override
			public void run(){
				try{
					HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
					con.setReadTimeout(5000);
					con.setConnectTimeout(5000);
					con.setRequestMethod("GET");
					if (con.getResponseCode() == 200) {
						InputStream is = con.getInputStream();
						FileOutputStream fileOutputStream = null;
						if (is != null) {
							fileOutputStream = new FileOutputStream(file);
							byte[] buf = new byte[1024];
							int ch;
							while ((ch = is.read(buf)) != -1) {
								fileOutputStream.write(buf, 0, ch);
							}
						}
						if (fileOutputStream != null) {
							fileOutputStream.flush();
							fileOutputStream.close();
						}
						runOnUiThread(() -> {
							if(downloadDialog.isShowing())downloadDialog.cancel();
							checkPackage();
						});
						return;
					}else if(con.getResponseCode() == 404){
						app.toast("新版本文件不存在！");
					}else app.toast("连接服务器失败！");
					
				}catch(IOException e){
					LogUtil.Log(e);
					app.toast("下载失败！");
				}
				if(downloadDialog.isShowing())runOnUiThread(() -> downloadDialog.cancel());
			}
		}.start();
	}
	
	private void checkPackage(){
		PackageManager pm = getPackageManager();
		PackageInfo packageInfo = pm.getPackageArchiveInfo(file.toString(), PackageManager.GET_ACTIVITIES);
		if(packageInfo != null){
			try{
				if(pm.getPackageInfo(getPackageName(), 0).packageName.equals(packageInfo.packageName) ||
						version == packageInfo.versionCode || FileUtil.getMD5(file).equals(md5)){
					installApk();
					return;
				}
			}catch(PackageManager.NameNotFoundException e){
				LogUtil.Log(e);
			}
		}
		askForReDownload();
	}
	
	private void askForReDownload(){
		new MaterialDialog.Builder(this)
				.title("校验失败")
				.content("安装包MD5校验失败，是否仍要安装？")
				.positiveText("安装").onPositive((d, which)-> installApk())
				.negativeText("取消").onNegative((d, which)-> d.dismiss())
				.neutralText("重新下载").onNeutral((d, which) -> downloadApk())
				.show();
	}
	
	private void installApk(){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri data;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			data = FileProvider.getUriForFile(this, getPackageName(), file);
			// 给目标应用一个临时授权
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		} else {
			data = Uri.fromFile(file);
		}
		intent.setDataAndType(data, "application/vnd.android.package-archive");
		startActivity(intent);
	}
}
