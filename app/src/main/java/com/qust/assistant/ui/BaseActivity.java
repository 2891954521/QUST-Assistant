package com.qust.assistant.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gyf.immersionbar.ImmersionBar;
import com.qust.assistant.R;

public class BaseActivity extends AppCompatActivity{

	private BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		initStatusBar();
		registerReceiver();
	}
	
	protected void initStatusBar(){
		ImmersionBar immersionBar = ImmersionBar.with(this).transparentBar();
		int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		if(mode == Configuration.UI_MODE_NIGHT_YES){
			// 夜间模式
			immersionBar.statusBarDarkFont(false);
			immersionBar.navigationBarDarkIcon(false);
		} else if(mode == Configuration.UI_MODE_NIGHT_NO){
			// 日间模式
			immersionBar.statusBarDarkFont(true);
			immersionBar.navigationBarDarkIcon(true);
		}
		immersionBar.init();
	}
	
	// 初始化ToolBar并设置标题
	protected void initToolBar(@Nullable String title){
		Toolbar toolbar = findViewById(R.id.toolbar);
		if(toolbar == null) return;
		if(title != null) toolbar.setTitle(title);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
	}
	
	// 重写此方法以注册广播监听
	protected void registerReceiver(){
	
	}
	
	//
	protected void registerReceiver(BroadcastReceiver receiver, @NonNull String... actions){
		this.receiver = receiver;
		IntentFilter f = new IntentFilter();
		for(String action : actions) f.addAction(action);
		registerReceiver(receiver, f);
	}
	
	// 销毁Activity时取消广播监听
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(receiver != null) unregisterReceiver(receiver);
	}
	
	public void toast(String message){
		toast(message,Toast.LENGTH_SHORT);
	}
	
	public void toast(String message,int time){
		Toast.makeText(this,message,time).show();
	}
	
}
