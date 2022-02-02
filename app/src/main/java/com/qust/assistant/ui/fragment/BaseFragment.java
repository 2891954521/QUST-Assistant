package com.qust.assistant.ui.fragment;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.widget.slide.DragType;
import com.qust.assistant.widget.slide.SlidingLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;

public abstract class BaseFragment{
	
	protected boolean isCreated;
	
	protected MainActivity activity;
	
	protected SlidingLayout rootView;
	
	protected Toolbar toolbar;
	
	public BaseFragment init(MainActivity activity, boolean isRoot){
		this.activity = activity;
		
		LayoutInflater layoutInflater = LayoutInflater.from(activity);
		
		rootView = (SlidingLayout)layoutInflater.inflate(R.layout.fragment_base, null);
		
		if(isRoot){
			rootView.setFocusable(false);
			rootView.setFocusableInTouchMode(false);
			rootView.setDragType(DragType.NONE);
		}else{
			rootView.setFocusable(true);
			rootView.setFocusableInTouchMode(true);
			rootView.setOnPageChangeListener(isBack -> { if(isBack) activity.onBackPressed(); });
		}
		
		((ViewGroup)rootView.findViewById(R.id.fragment_base_content)).addView(layoutInflater.inflate(getLayout(), null));

		toolbar = rootView.findViewById(R.id.toolbar);
		
		if(toolbar != null){
			toolbar.setTitle(getName());
			if(isRoot){
				toolbar.setNavigationIcon(R.drawable.ic_menu);
				toolbar.setNavigationOnClickListener(v -> activity.openMenu());
			}else{
				toolbar.setNavigationOnClickListener(v -> finish());
			}
		}
		
		initLayout(layoutInflater);
		
		isCreated = true;
		
		return this;
	}

	public void onResume(){ }
	
	public void onReceive(String msg){ }
	
	public void onResult(int requestCode, int resultCode, Intent data){ }
	
	protected void setSlidingParam(SlidingLayout.onAnimListener anim, View disallowView){
		if(anim != null) rootView.setOnAnimListener(anim);
		rootView.setDisallowView(disallowView);
	}
	
	/**
	 * 给 ToolBar 添加按键
	 * @param layoutInflater -
	 * @param icon 图标
	 * @param listener 点击事件
	 * @return imageView
	 */
	protected ImageView addMenuItem(LayoutInflater layoutInflater, @DrawableRes int icon, View.OnClickListener listener){
		ImageView imageView = (ImageView)layoutInflater.inflate(R.layout.view_image, toolbar, false);
		imageView.setImageResource(icon);
		imageView.setOnClickListener(listener);
		
		((Toolbar.LayoutParams)imageView.getLayoutParams()).gravity = Gravity.CENTER | Gravity.END;
		
		toolbar.addView(imageView);
		return imageView;
	}
	
	protected abstract void initLayout(LayoutInflater inflater);
	
	/**
	 * 获取 Fragment 布局
	 * @return 布局文件 id
	 */
	protected abstract int getLayout();
	
	/**
	 * 获取 Fragment 的名字，用于显示在 ToolBar 上
	 * @return name
	 */
	protected abstract String getName();
	
	/**
	 * 返回键按下的处理
	 * @return 是否返回
	 */
	public boolean onBackPressed(){ return true; }
	
	public View getView(){ return rootView; }
	
	public void finish(){ activity.removeTopView(); }
	
	protected <T extends View> T findViewById(@IdRes int id){ return rootView.findViewById(id); }
	
	protected void toast(String msg){ activity.toast(msg); }
	
}
