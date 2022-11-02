package com.qust.assistant.ui.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;
import com.qust.assistant.ui.BaseAnimActivity;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.UpdateUtil;
import com.qust.assistant.vo.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateActivity extends BaseAnimActivity{
	
	private MaterialDialog checkDialog;
	
	private MaterialDialog downloadDialog;
	
	private UpdateInfo info;
	
	private File file;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		
		file = new File(getExternalCacheDir(),"release.apk");
		
		checkDialog = DialogUtil.getIndeterminateProgressDialog(this,"正在检查更新").build();
		checkDialog.setCanceledOnTouchOutside(false);
		
		downloadDialog = DialogUtil.getIndeterminateProgressDialog(this, "正在下载").build();
		downloadDialog.setCanceledOnTouchOutside(false);
		
		try{
			PackageInfo pkg = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.activity_update_current_version)).setText("当前版本：" + pkg.versionName);
		}catch(PackageManager.NameNotFoundException ignore){ }
		
		initToolBar(null);
		
		View view = findViewById(R.id.activity_update_button);
		view.setOnClickListener(v -> {
			if(info == null){
				checkUpdate();
			}else{
				downloadApk();
			}
		});
		view.postDelayed(this::checkUpdate,200);
	}
	
	/**
	 * 检查更新
	 */
	private void checkUpdate(){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isDev = setting.getBoolean("key_update_dev",false);
		checkDialog.show();
		new Thread(){
			@Override
			public void run(){
				info = UpdateUtil.checkVersion(UpdateActivity.this, isDev);
				runOnUiThread(() -> {
					if(info == null){
						toast("当前无新版本！");
					}else{
						((TextView)findViewById(R.id.activity_update_info)).setText("下载地址：" + info.apkUrl + "\n" + info.message);
					}
					checkDialog.cancel();
				});
			}
		}.start();
	}
	
	private void downloadApk(){
		downloadDialog.show();
		new Thread(){
			@Override
			public void run(){
				try{
					HttpURLConnection con = (HttpURLConnection)new URL(info.apkUrl).openConnection();
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
						toast("新版本文件不存在！");
					}else toast("连接服务器失败！");
					
				}catch(IOException e){
					LogUtil.Log(e);
					toast("下载失败！");
				}
				if(downloadDialog.isShowing())runOnUiThread(() -> downloadDialog.cancel());
			}
		}.start();
	}
	
	/**
	 * 检查下载的apk信息
	 */
	private void checkPackage(){
		PackageManager pm = getPackageManager();
		PackageInfo packageInfo = pm.getPackageArchiveInfo(file.toString(), PackageManager.GET_ACTIVITIES);
		if(packageInfo == null){
			toast("获取更新包信息失败！");
			return;
		}
		try{
			// 校验一下包名
			if(pm.getPackageInfo(getPackageName(), 0).packageName.equals(packageInfo.packageName)){
				installApk();
			}else{
				askForReDownload();
			}
		}catch(PackageManager.NameNotFoundException e){
			LogUtil.Log(e);
		}
	}
	
	private void askForReDownload(){
		new MaterialDialog.Builder(this)
				.title("校验失败")
				.content("安装包异常，是否仍要安装？")
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
