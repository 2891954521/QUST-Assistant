package com.qust.assistant.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.qust.assistant.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity{

	private BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		registerReceiver();
	}
	
	protected void initStatusBar() {
		
		View decorView = getWindow().getDecorView();
		
		boolean isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode();
		
		if(isInMultiWindowMode){
			// 多窗口模式下不修改状态栏
		}else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}else{
			int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
				if(mode == Configuration.UI_MODE_NIGHT_YES) {
					systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
				} else if(mode == Configuration.UI_MODE_NIGHT_NO) {
					systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
				}
			}

			decorView.setSystemUiVisibility(systemUiVisibility);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
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
