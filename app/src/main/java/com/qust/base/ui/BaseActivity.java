package com.qust.base.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	public final void toastOK(String message){
		toast(R.drawable.tips_finish, message);
	}
	
	public final void toastWarning(String message){
		toast(R.drawable.tips_warning, message);
	}
	public final void toastError(String message){
		toast(R.drawable.tips_error, message);
	}
	
	private void toast(@DrawableRes int icon, String message){
		FrameLayout layout = (FrameLayout)LayoutInflater.from(this).inflate(R.layout.layout_tips, null);
		((ImageView)layout.findViewById(R.id.tips_icon)).setImageResource(icon);
		((TextView)layout.findViewById(R.id.tips_message)).setText(message);
		Toast toast = new Toast(this);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
