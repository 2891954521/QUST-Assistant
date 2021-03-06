package com.qust.assistant.ui.fragment;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer;
import com.billy.android.swipe.listener.SimpleSwipeListener;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.layout.BaseLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.Toolbar;

public abstract class BaseFragment implements BaseLayout{
	
	protected MainActivity activity;
	
	protected ViewGroup rootView;
	
	protected ViewGroup layout;
	
	protected Toolbar toolbar;
	
	protected TranslucentSlidingConsumer consumer;
	
	public BaseFragment(MainActivity activity){
		this.activity = activity;
	}
	
	public BaseFragment init(boolean isRoot){
		
		LayoutInflater layoutInflater = LayoutInflater.from(activity);
		
		consumer = SmartSwipe.wrap(layoutInflater.inflate(R.layout.fragment_base, null))
				.addConsumer(new TranslucentSlidingConsumer());
		
		if(!isRoot){
			consumer.enableLeft();
		}
		
		consumer.addListener(new SimpleSwipeListener(){
			@Override
			public void onSwipeOpened(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction) {
				activity.onBackPressed();
			}
		});
		
		rootView = consumer.getWrapper();
		
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
		
		if(layout == null) initLayout(layoutInflater);
		
		((ViewGroup)rootView.findViewById(R.id.fragment_base_content)).addView(layout);
		
		return this;
	}
	
	public void onPause(){ };
	
	public void onResume(){ }
	
	/**
	 * ????????????
	 * @param msg
	 */
	public void onReceive(String msg){ }
	
	/**
	 * ?????? onActivityResult
	 */
	public void onResult(int requestCode, int resultCode, Intent data){ }
	
	/**
	 * ??? ToolBar ????????????
	 * @param layoutInflater -
	 * @param icon ??????
	 * @param listener ????????????
	 * @return imageView
	 */
	protected ImageView addMenuItem(LayoutInflater layoutInflater, @DrawableRes int icon, View.OnClickListener listener){
		ImageView imageView = (ImageView)layoutInflater.inflate(R.layout.view_image, toolbar, false);
		imageView.setImageResource(icon);
		imageView.setOnClickListener(listener);
		
		if(toolbar != null){
			((Toolbar.LayoutParams)imageView.getLayoutParams()).gravity = Gravity.CENTER | Gravity.END;
			toolbar.addView(imageView);
		}
		
		return imageView;
	}
	
	protected void initLayout(LayoutInflater inflater){
		layout = (ViewGroup)inflater.inflate(getLayoutId(), null);
	}
	
	/**
	 * ?????? Fragment ??????
	 * @return ???????????? id
	 */
	@LayoutRes
	protected abstract int getLayoutId();
	
	/**
	 * ?????? Fragment ??????????????????????????? ToolBar ???
	 * @return name
	 */
	protected abstract String getName();
	
	/**
	 * ????????????????????????
	 * @return ????????????
	 */
	public boolean onBackPressed(){ return true; }
	
	public final View getView(){ return rootView; }
	
	@Override
	public final View getLayout(){
		if(layout == null) initLayout(LayoutInflater.from(activity));
		
		return layout;
	}
	
	public boolean isCreated(){
		return layout != null;
	}
	
	public void finish(){ activity.removeTopView(); }
	
	protected <T extends View> T findViewById(@IdRes int id){ return layout.findViewById(id); }
	
	protected void toast(String msg){ activity.toast(msg); }
	
}
