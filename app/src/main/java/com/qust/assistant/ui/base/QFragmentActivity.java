package com.qust.assistant.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.SpaceConsumer;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.HomeFragment;
import com.qust.assistant.widget.swipe.MainDrawer;

import java.util.Stack;

/**
 * 含有Fragment容器和侧滑菜单的Activity
 */
public class QFragmentActivity extends BaseActivity{
	
	private boolean isFading;
	
	private MainDrawer drawer;
	
	private FragmentManager fragmentManager;
	
	private Stack<BaseFragment> fragments;
	
	public Animation animIn, animOut;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fragments = new Stack<>();
		fragmentManager = getSupportFragmentManager();
		initHome();
		initAnim();
		initDrawer();
	}
	
	/**
	 * 初始化侧滑菜单
	 */
	private void initDrawer(){
		drawer = SmartSwipe.wrap(this).addConsumer(new MainDrawer());
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		View drawerView = LayoutInflater.from(this).inflate(R.layout.nav_main, drawer.getWrapper(), false);
		
		drawerView.setLayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels / 4 * 3, ViewGroup.LayoutParams.MATCH_PARENT));
		
		drawer.setHorizontalDrawerView(drawerView).setScrimColor(0x2F000000).disableRight();
		
		SmartSwipe.wrap(drawerView.findViewById(R.id.nav_main_menu)).addConsumer(new SpaceConsumer()).enableVertical();
	}
	
	/**
	 * 初始化Fragment动画
	 */
	private void initAnim(){
		animIn = AnimationUtils.loadAnimation(this, R.anim.anim_right_in);
		animIn.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationEnd(Animation param1Animation){
				isFading = false;
				drawer.enableRight();
				drawer.disableLeft();
			}
			@Override
			public void onAnimationRepeat(Animation param1Animation){}
			@Override
			public void onAnimationStart(Animation param1Animation){}
		});
		
		animOut = AnimationUtils.loadAnimation(this, R.anim.anim_rigth_out);
		animOut.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationEnd(Animation paramAnimation){
				fragments.pop();
				fragments.peek().getView().setFocusable(true);
				fragments.peek().getView().setClickable(true);
				
				if(fragments.size() > 1){
					fragments.get(fragments.size() - 2).getView().setVisibility(View.VISIBLE);
				}else{
					drawer.enableLeft();
					drawer.disableRight();
				}
				isFading = false;
			}
			@Override
			public void onAnimationRepeat(Animation param1Animation){}
			@Override
			public void onAnimationStart(Animation param1Animation){}
		});
	}
	
	/**
	 * 初始化主页
	 */
	private void initHome(){
		BaseFragment home = new HomeFragment(true, true);
		fragments.push(home);
		fragmentManager.beginTransaction().add(R.id.main_frame, home, home.getClass().getName()).commit();
	}
	
	/**
	 * 添加一个Fragment
	 */
	public synchronized void addView(Class<? extends BaseFragment> newFragment){
		if(isFading) return;
		try{
			if(fragments.size() > 1) fragments.get(fragments.size() - 2).getView().setVisibility(View.GONE);
			
			BaseFragment a = fragments.peek();
			a.getView().setFocusable(false);
			a.getView().setClickable(false);
			
			drawer.close();
			
			BaseFragment b = newFragment.newInstance();
			
			fragments.push(b);
			
			fragmentManager.beginTransaction()
					.setCustomAnimations(R.anim.anim_right_in, 0)
					.add(R.id.main_frame, b, b.getClass().getName())
					.addToBackStack("")
					.commit();
			
			isFading = true;
		}catch(ReflectiveOperationException ignored){ }
	}
	
	/**
	 * 移除顶部Fragment (相当于返回)
	 */
	public synchronized void removeTopView(){
		if(!isFading && fragments.size() > 1){
			isFading = true;
			fragmentManager.beginTransaction()
					.setCustomAnimations(0, R.anim.anim_rigth_out)
					.remove(fragments.peek())
					.commit();
		}
	}
	
	public void openMenu(){ drawer.open(true, SwipeConsumer.DIRECTION_LEFT); }
	
	public void closeMenu(){ drawer.close(true); }
	
	@Override
	public void onBackPressed(){
		if(isFading) return;
		if(drawer.isOpened()){
			drawer.close(true);
		}else if(fragments.peek().onBackPressed()){
			if(fragments.size() == 1){
				super.onBackPressed();
			}else{
				removeTopView();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		fragments.peek().onResult(requestCode, resultCode, data);
	}
}
