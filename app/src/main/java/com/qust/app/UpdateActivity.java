package com.qust.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.vo.UpdateInfo;
import com.qust.base.ui.BaseAnimActivity;
import com.qust.utils.UpdateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateActivity extends BaseAnimActivity{
	
	private File file;
	
	private UpdateInfo info;
	
	private Button updateButton;
	
	private NumberPicker channelPicker;
	
	private MaterialDialog checkDialog;
	
	private MaterialDialog downloadDialog;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		
		initToolBar(null);
		
		file = new File(getExternalCacheDir(),"release.apk");

		updateButton = findViewById(R.id.activity_update_update);
		channelPicker = findViewById(R.id.activity_update_channel);
		
		checkDialog = DialogUtil.getIndeterminateProgressDialog(this, getString(R.string.text_checking_version)).build();
		checkDialog.setCanceledOnTouchOutside(false);
		
		downloadDialog = DialogUtil.getIndeterminateProgressDialog(this, getString(R.string.text_downloading)).build();
		downloadDialog.setCanceledOnTouchOutside(false);
		
		boolean isDev = SettingUtil.getBoolean(getString(R.string.KEY_UPDATE_DEV), false);
		String[] channel = isDev ? new String[] { "Gitee", "Github", "备用源", "开发版" } : new String[] { "Gitee", "Github", "备用源" };
		channelPicker.setWrapSelectorWheel(false);
		channelPicker.setMaxValue(channel.length - 1);
		channelPicker.setMinValue(0);
		channelPicker.setDisplayedValues(channel);
		channelPicker.setValue(isDev ? 3 : 0);
		
		try{
			PackageInfo pkg = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.activity_update_current_version)).setText(getString(R.string.text_current_version, pkg.versionName));
		}catch(PackageManager.NameNotFoundException ignore){ }
		
		findViewById(R.id.activity_update_check).setOnClickListener(v -> checkUpdate());
		
		updateButton.setOnClickListener(v -> downloadApk());
		
		updateButton.postDelayed(this::checkUpdate,200);
	}
	
	/**
	 * 检查更新
	 */
	private void checkUpdate(){
		checkDialog.show();
		new Thread(){
			@Override
			public void run(){
				switch(channelPicker.getValue()){
					case 0:
						info = UpdateUtils.checkVersionFromGit(UpdateUtils.GITEE_UPDATE_URL); break;
					case 1:
						info = UpdateUtils.checkVersionFromGit(UpdateUtils.GITHUB_UPDATE_URL); break;
					case 2:
						info = UpdateUtils.checkVersion(UpdateActivity.this, false); break;
					case 3:
						info = UpdateUtils.checkVersion(UpdateActivity.this, true); break;
				}
				runOnUiThread(() -> {
					if(info == null){
						toast("当前无新版本");
						updateButton.setVisibility(View.GONE);
					}else{
						((TextView)findViewById(R.id.activity_update_info)).setText(getString(R.string.text_download_url, info.apkUrl, info.message));
						updateButton.setVisibility(View.VISIBLE);
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
					if(con.getResponseCode() == 200){
						InputStream is = con.getInputStream();
						FileOutputStream fileOutputStream = null;
						if(is != null){
							fileOutputStream = new FileOutputStream(file);
							byte[] buf = new byte[1024];
							int ch;
							while((ch = is.read(buf)) != -1){
								fileOutputStream.write(buf, 0, ch);
							}
						}
						if(fileOutputStream != null){
							fileOutputStream.flush();
							fileOutputStream.close();
						}
						runOnUiThread(() -> {
							if(downloadDialog.isShowing()) downloadDialog.cancel();
							checkPackage();
						});
						return;
					}else if(con.getResponseCode() == 404){
						runOnUiThread(() -> toast("新版本文件不存在"));
					}else{
						runOnUiThread(() -> toast("连接服务器失败"));
					}
					
				}catch(IOException ignore){
					runOnUiThread(() -> toast("下载失败"));
				}
				if(downloadDialog.isShowing()) runOnUiThread(() -> downloadDialog.cancel());
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
			runOnUiThread(() -> toast("获取更新包信息失败！"));
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
		DialogUtil.getBaseDialog(this).title("校验失败")
				.content("安装包异常，是否仍要安装？")
				.positiveText("安装").onPositive((d, which)-> installApk())
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
