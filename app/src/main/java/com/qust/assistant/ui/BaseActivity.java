package com.qust.assistant.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.qust.assistant.R;
import com.qust.assistant.widget.slide.SlidingLayout;

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
	
	// 重写此方法以注册广播监听
	protected void registerReceiver(){
	
	}
	
	//
	protected void registerReceiver(BroadcastReceiver receiver,String... actions){
		this.receiver = receiver;
		IntentFilter f = new IntentFilter();
		for(String action : actions) f.addAction(action);
		registerReceiver(receiver,f);
	}
	
	public void toast(String message){
		toast(message,Toast.LENGTH_SHORT);
	}
	
	public void toast(String message,int time){
		Toast.makeText(this,message,time).show();
	}
	
	// 初始化滑动返回
	protected void initSliding(@Nullable SlidingLayout.onAnimListener listener, @Nullable View disallowView){
		SlidingLayout slidingLayout = findViewById(R.id.slidingLayout);
		if(slidingLayout == null) return;
		if(listener != null)slidingLayout.setOnAnimListener(listener);
		slidingLayout.setDisallowView(disallowView);
		slidingLayout.setOnPageChangeListener(isBack -> {
			if(isBack) finish();
		});
	}
	
	// 初始化ToolBar并设置标题
	protected void initToolBar(@Nullable String title){
		Toolbar toolbar = findViewById(R.id.toolbar);
		if(toolbar == null) return;
		if(title != null) toolbar.setTitle(title);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
	}
	
	// 销毁Activity时取消广播监听
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(receiver!=null) unregisterReceiver(receiver);
	}
	
}
