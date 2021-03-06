package com.qust.assistant.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.SpaceConsumer;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.ui.fragment.HomeFragment;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.UpdateUtil;
import com.qust.assistant.widget.swipe.MainDrawer;

import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity{
	
	private static final int NOT_NOTICE = 2;
	
	private Stack<BaseFragment> fragments;
	
	private MainDrawer drawer;
	
	private FrameLayout layout;
	
	private Animation animFadeOut;
	
	private Animation animIn, animOut;
	
	private boolean isFading;
	
	@Override
	protected void onCreate(Bundle paramBundle){
		super.onCreate(paramBundle);
		
		setContentView(R.layout.activity_main);
		
		initStatusBar();
		
		fragments = new Stack<>();
		
		layout = findViewById(R.id.main_frame);
		
		initDrawer();

		initHome();
		
		initAnim();
		
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
			ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
		}
		
		UpdateUtil.checkUpdate(this);
	}
	
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
			public void onAnimationEnd(Animation param1Animation){
				layout.removeView(fragments.pop().getView());
				
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
		
		animFadeOut = AnimationUtils.loadAnimation((Context)this, R.anim.anim_fade_out);
	}
	
	private void initHome(){
		BaseFragment home;
		
		try{
			Class<?> object = Class.forName(SettingUtil.setting.getString("defaultHome", HomeFragment.class.getName()));
			if(BaseFragment.class.isAssignableFrom(object)){
				home = ((BaseFragment)object.getConstructor(MainActivity.class).newInstance(this)).init(true);
			}else{
				home = new HomeFragment(this).init(true);
			}
		}catch(ReflectiveOperationException e){
			home = new HomeFragment(this).init(true);
		}
		
		fragments.push(home);
		
		layout.addView(home.getView());
	}
	
	public synchronized void addView(Class<? extends BaseFragment> newFragment){
		if(isFading) return;
		try{
			if(fragments.size() > 1) fragments.get(fragments.size() - 2).getView().setVisibility(View.GONE);

			BaseFragment a = fragments.peek();
			a.getView().setFocusable(false);
			a.getView().setClickable(false);
			
			drawer.close();

			BaseFragment b = ((BaseFragment)newFragment.getConstructor(MainActivity.class).newInstance(this)).init(false);
			
			fragments.push(b);
			layout.addView(b.getView());
			
			a.getView().startAnimation(animFadeOut);
			b.getView().startAnimation(animIn);
			
			isFading = true;
		}catch(ReflectiveOperationException ignored){ }
	}
	
	public synchronized void removeTopView(){
		if(!isFading && fragments.size() > 1){
			isFading = true;
			fragments.peek().getView().startAnimation(animOut);
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
	
	@Override
	protected void registerReceiver(){
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent){
				if(intent.getAction() == null) return;
				for(BaseFragment fragment : fragments) fragment.onReceive(intent.getAction());
			}
		}, App.APP_UPDATE_LESSON_TABLE);
	}
	
	public void openMenu(){ drawer.open(true, SwipeConsumer.DIRECTION_LEFT); }
	
	public void closeMenu(){ drawer.close(true); }
	
	@Override
	public void onResume(){
		super.onResume();
		fragments.peek().onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == NOT_NOTICE){
			// ????????????????????????????????????????????????????????????
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
			}
		}else{
			fragments.peek().onResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == 1){
			for(int i = 0; i < permissions.length; i++){
				if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
					if(!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){
						// ?????????????????????????????????
						new MaterialDialog.Builder(this).title("??????").content("??????????????????????????????????????????")
							.positiveText(R.string.text_cancel).onPositive((dialog, which) -> {
								toast("??????????????????????????????????????????");
								finish();
						}).negativeText(R.string.text_ok).onNegative((dialog, which) -> {
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								// ????????????"package",???????????????????????????
								Uri uri = Uri.fromParts("package", getPackageName(), null);
								intent.setData(uri);
								startActivityForResult(intent, NOT_NOTICE);
								dialog.dismiss();
							}).show();
						break;
					}else{
						// ????????????
						new MaterialDialog.Builder(this).title("??????").content("??????????????????????????????????????????")
							.positiveText(R.string.text_cancel).onPositive((dialog, which) -> {
								toast("??????????????????????????????????????????");
								finish();
							}).negativeText(R.string.text_ok).onNegative((dialog, which) -> {
								ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
								dialog.dismiss();
							}).show();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
}