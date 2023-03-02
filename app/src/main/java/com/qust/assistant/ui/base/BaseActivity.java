package com.qust.assistant.ui.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gyf.immersionbar.ImmersionBar;
import com.qust.assistant.R;

public class BaseActivity extends AppCompatActivity{

	private Toolbar toolbar;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		initStatusBar();
	}
	
	/**
	 * 初始化状态栏
	 */
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
	
	/**
	 * 初始化ToolBar并设置标题
	 */
	protected void initToolBar(@Nullable String title){
		toolbar = findViewById(R.id.toolbar);
		if(toolbar == null) return;
		if(title != null) toolbar.setTitle(title);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
	}
	
	/**
	 * 给 ToolBar 添加按键
	 * @param icon 图标
	 * @param listener 点击事件
	 * @return imageView
	 */
	@NonNull
	protected final ImageView addMenuItem(@DrawableRes int icon, View.OnClickListener listener){
		ImageView imageView = (ImageView)LayoutInflater.from(this).inflate(R.layout.view_image, toolbar, false);
		imageView.setImageResource(icon);
		imageView.setOnClickListener(listener);
		
		if(toolbar != null){
			((Toolbar.LayoutParams)imageView.getLayoutParams()).gravity = Gravity.CENTER | Gravity.END;
			toolbar.addView(imageView);
		}
		
		return imageView;
	}
	
	public void toast(String message){
		toast(message, Toast.LENGTH_SHORT);
	}
	
	public void toast(String message, int time){
		Toast.makeText(this, message, time).show();
	}
	
}
