package com.qust.assistant.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.TranslucentSlidingConsumer;
import com.billy.android.swipe.listener.SimpleSwipeListener;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;

public abstract class BaseFragment extends Fragment{
	
	protected QFragmentActivity activity;
	
	protected ViewGroup layout;
	
	protected Toolbar toolbar;
	
	protected boolean isRoot;
	
	protected boolean hasToolBar;
	
	public BaseFragment(){
		this(false, true);
	}
	
	/**
	 * @param isRoot 是否为根Fragment，为true时不能够滑动返回
	 * @param hasToolBar 是否有标题栏，决定在显示Fragment时需不需要添加BaseLayout
	 */
	public BaseFragment(boolean isRoot, boolean hasToolBar){
		this.isRoot = isRoot;
		this.hasToolBar = hasToolBar;
	}
	
	@Override
	public void onAttach(@NonNull Context context){
		super.onAttach(context);
		activity = (MainActivity) getActivity();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		if(!isRoot){
			TranslucentSlidingConsumer consumer = SmartSwipe.wrap(inflater.inflate(hasToolBar ? R.layout.fragment_base : getLayoutId(), null))
					.addConsumer(new TranslucentSlidingConsumer());
			consumer.enableLeft();
			consumer.addListener(new SimpleSwipeListener(){
				@Override
				public void onSwipeOpened(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction){
					activity.onBackPressed();
				}
			});
			layout = consumer.getWrapper();
			
			if(hasToolBar){
				initToolBar();
				((ViewGroup)layout.findViewById(R.id.fragment_base_content)).addView(inflater.inflate(getLayoutId(), null));
			}
		}else{
			if(hasToolBar){
				layout = (ViewGroup)inflater.inflate(R.layout.fragment_base, null);
				initToolBar();
				((ViewGroup)layout.findViewById(R.id.fragment_base_content)).addView(inflater.inflate(getLayoutId(), null));
			}else{
				layout = (ViewGroup)inflater.inflate(getLayoutId(), null);
			}
		}
		initLayout(inflater);
		return layout;
	}
	
	@Nullable
	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim){
		if(nextAnim == R.anim.anim_right_in){
			return activity.animIn;
		}else if(nextAnim == R.anim.anim_rigth_out){
			return activity.animOut;
		}else{
			return super.onCreateAnimation(transit, enter, nextAnim);
		}
	}
	
	/**
	 * 初始化ToolBar
	 */
	protected final void initToolBar(){
		toolbar = layout.findViewById(R.id.toolbar);
		if(toolbar != null){
			toolbar.setTitle(getName());
			if(isRoot){
				toolbar.setNavigationIcon(R.drawable.ic_menu);
				toolbar.setNavigationOnClickListener(v -> activity.openMenu());
			}else{
				toolbar.setNavigationOnClickListener(v -> finish());
			}
		}
	}
	
	/**
	 * 初始化 Fragment View
	 */
	protected abstract void initLayout(LayoutInflater inflater);
	
	/**
	 * 获取 Fragment 布局
	 * @return 布局文件 id
	 */
	@LayoutRes
	protected abstract int getLayoutId();
	
	/**
	 * 获取 Fragment 的名字，用于显示在 ToolBar 上
	 * @return name
	 */
	public abstract String getName();
	
	/**
	 * 来自 onActivityResult
	 */
	public void onResult(int requestCode, int resultCode, Intent data){ }
	
	/**
	 * 返回键按下的处理
	 * @return 是否返回
	 */
	public boolean onBackPressed(){ return true; }
	
	/**
	 * 关闭这个Fragment
	 */
	public void finish(){ activity.removeTopView(); }
	
	/**
	 * 给 ToolBar 添加按键
	 * @param layoutInflater -
	 * @param icon 图标
	 * @param listener 点击事件
	 * @return imageView
	 */
	@NonNull
	protected final ImageView addMenuItem(LayoutInflater layoutInflater, @DrawableRes int icon, View.OnClickListener listener){
		ImageView imageView = (ImageView)layoutInflater.inflate(R.layout.view_image, toolbar, false);
		imageView.setImageResource(icon);
		imageView.setOnClickListener(listener);
		
		if(toolbar != null){
			((Toolbar.LayoutParams)imageView.getLayoutParams()).gravity = Gravity.CENTER | Gravity.END;
			toolbar.addView(imageView);
		}
		
		return imageView;
	}
	
	public final boolean isCreated(){
		return layout != null;
	}
	
	protected final <T extends View> T findViewById(@IdRes int id){ return layout.findViewById(id); }
	
	protected final void toast(String msg){ activity.toast(msg); }
	
}
