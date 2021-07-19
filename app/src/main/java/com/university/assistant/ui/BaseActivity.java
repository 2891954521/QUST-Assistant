package com.university.assistant.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
	
	// 销毁Activity时取消广播监听
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(receiver!=null) unregisterReceiver(receiver);
	}
	
}
