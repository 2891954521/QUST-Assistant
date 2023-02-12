package com.qust.assistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.FragmentManager;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.SpaceConsumer;
import com.qust.assistant.R;
import com.qust.assistant.ui.app.GuideActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.ui.fragment.HomeFragment;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.UpdateUtil;
import com.qust.assistant.widget.swipe.MainDrawer;

import java.util.Stack;

public class MainActivity extends BaseActivity{
	
	private Stack<BaseFragment> fragments;
	
	private MainDrawer drawer;
	
	private boolean isFading;
	
	private FragmentManager fragmentManager;
	
	public Animation animIn, animOut;
	
	@Override
	protected void onCreate(Bundle paramBundle){
		super.onCreate(paramBundle);
		
		// 第一次使用跳转到引导页
		if(SettingUtil.getBoolean(SettingUtil.IS_FIRST_USE, true)){
			startActivity(new Intent(this, GuideActivity.class));
			finish();
			return;
		}
		
		setContentView(R.layout.activity_main);
		
		fragments = new Stack<>();
		
		fragmentManager = getSupportFragmentManager();
		
		initDrawer();

		initHome();
		
		initAnim();
		
		// 检查更新
		UpdateUtil.checkUpdate(this);
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
		BaseFragment home;
		
		try{
			Class<?> object = Class.forName(SettingUtil.getString(SettingUtil.HOME_PAGE, HomeFragment.class.getName()));
			if(BaseFragment.class.isAssignableFrom(object)){
				home = ((BaseFragment)object.getConstructor(Boolean.class, Boolean.class).newInstance(true, true));
			}else{
				home = new HomeFragment(true, true);
			}
		}catch(ReflectiveOperationException e){
			home = new HomeFragment(true, true);
		}
		
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
	
	@Override
	public void onBackPressed(){
		if(isFading){
			// Do nothing
		}else if(drawer.isOpened()){
			drawer.close(true);
		}else if(fragments.peek().onBackPressed()){
			if(fragments.size() == 1){
				super.onBackPressed();
			}else{
				removeTopView();
			}
		}
	}
	
	public void openMenu(){ drawer.open(true, SwipeConsumer.DIRECTION_LEFT); }
	
	public void closeMenu(){ drawer.close(true); }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		fragments.peek().onResult(requestCode, resultCode, data);
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
}