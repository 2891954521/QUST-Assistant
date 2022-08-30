package com.qust.assistant.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.consumer.SpaceConsumer;
import com.qust.assistant.R;
import com.qust.assistant.ui.app.GuideActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.ui.fragment.HomeFragment;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.UpdateUtil;

import java.lang.reflect.Field;
import java.util.Stack;

public class MainActivity extends BaseActivity{
	
	private static final int NOT_NOTICE = 2;
	
	private Stack<BaseFragment> fragments;
	
	private DrawerLayout drawer;
	
	private FrameLayout layout;
	
	private Animation animFadeOut;
	
	private Animation animIn, animOut;
	
	private boolean isFading;
	
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
		
		layout = findViewById(R.id.main_frame);
		
		initDrawer();

		initHome();
		
		initAnim();
		
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
			ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
		}
		
		// 检查更新
		UpdateUtil.checkUpdate(this);
	}
	
	/**
	 * 初始化侧滑菜单
	 */
	private void initDrawer(){
		
		drawer = findViewById(R.id.drawerLayout);
		
		try{
			Field leftDraggerField = drawer.getClass().getDeclaredField("mLeftDragger");
			leftDraggerField.setAccessible(true);
			ViewDragHelper leftDragger = (ViewDragHelper)leftDraggerField.get(drawer);

			// 找到 edgeSizeField 并设置 Accessible 为true
			Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
			edgeSizeField.setAccessible(true);
			int edgeSize = edgeSizeField.getInt(leftDragger);

			// 设置新的边缘大小
			Point displaySize = new Point();
			getWindowManager().getDefaultDisplay().getSize(displaySize);
			edgeSizeField.setInt(leftDragger, Math.max(edgeSize, displaySize.x));
		}catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e){
			e.printStackTrace();
		}

//		DisplayMetrics displayMetrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//		findViewById(R.id.nav_main).setLayoutParams(new DrawerLayout.LayoutParams(displayMetrics.widthPixels / 4 * 3, DrawerLayout.LayoutParams.MATCH_PARENT));

		// 侧滑菜单里垂直滑动弹性效果
		SmartSwipe.wrap(findViewById(R.id.nav_main_menu)).addConsumer(new SpaceConsumer()).enableVertical();
	}
	
	private void initAnim(){
		
		animIn = AnimationUtils.loadAnimation(this, R.anim.anim_right_in);
		animIn.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationEnd(Animation param1Animation){
				isFading = false;
				drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
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
				layout.removeView(fragments.pop().getView());
				
				fragments.peek().getView().setFocusable(true);
				fragments.peek().getView().setClickable(true);
				
				if(fragments.size() > 1){
					fragments.get(fragments.size() - 2).getView().setVisibility(View.VISIBLE);
				}else{
					drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
				}
				
				isFading = false;
			}
			
			@Override
			public void onAnimationRepeat(Animation param1Animation){}
			
			@Override
			public void onAnimationStart(Animation param1Animation){}
		});
		
		animFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
	}
	
	private void initHome(){
		BaseFragment home;
		
		try{
			Class<?> object = Class.forName(SettingUtil.getString(SettingUtil.HOME_PAGE, HomeFragment.class.getName()));
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
			
			drawer.closeDrawer(GravityCompat.START);

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
		}else if(drawer.isDrawerOpen(GravityCompat.START)){
			drawer.closeDrawer(GravityCompat.START);
		}else if(fragments.peek().onBackPressed()){
			if(fragments.size() == 1){
				super.onBackPressed();
			}else{
				removeTopView();
			}
		}
	}
	
	public void openMenu(){
		drawer.openDrawer(GravityCompat.START);
	}
	
	public void closeMenu(){
		drawer.closeDrawer(GravityCompat.START);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		fragments.peek().onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == NOT_NOTICE){
			// 由于不知道是否选择了允许所以需要再次判断
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
						// 用户选择了禁止不再询问
						new MaterialDialog.Builder(this).title("权限").content("请给予应用运行所必要的权限！")
							.positiveText(R.string.text_cancel).onPositive((dialog, which) -> {
								toast("请给予应用运行所必要的权限！");
								finish();
						}).negativeText(R.string.text_ok).onNegative((dialog, which) -> {
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								// 注意就是"package",不用改成自己的包名
								Uri uri = Uri.fromParts("package", getPackageName(), null);
								intent.setData(uri);
								startActivityForResult(intent, NOT_NOTICE);
								dialog.dismiss();
							}).show();
						break;
					}else{
						// 选择禁止
						new MaterialDialog.Builder(this).title("权限").content("请给予应用运行所必要的权限！")
							.positiveText(R.string.text_cancel).onPositive((dialog, which) -> {
								toast("请给予应用运行所必要的权限！");
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