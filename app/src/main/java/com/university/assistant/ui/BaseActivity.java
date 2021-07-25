package com.university.assistant.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.university.assistant.R;
import com.university.assistant.widget.SlidingLayout;

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
	protected void initSliding(@Nullable SlidingLayout.OnAnimListener listener,@Nullable View disallowView){
		SlidingLayout slidingLayout = findViewById(R.id.slidingLayout);
		if(slidingLayout==null) return;
		if(listener!=null)slidingLayout.setOnAnimListener(listener);
		slidingLayout.setDisallowView(disallowView);
		slidingLayout.setOnPageChangeListener(new SlidingLayout.OnPageChangeListener(){
			@Override public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){
				if(position==1)finish();
			}
			@Override public void isBack(boolean isBack){ }
		});
	}
	
	// 初始化ToolBar并设置标题
	protected void initToolBar(@Nullable String title){
		Toolbar toolbar = findViewById(R.id.toolbar);
		if(toolbar==null) return;
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
