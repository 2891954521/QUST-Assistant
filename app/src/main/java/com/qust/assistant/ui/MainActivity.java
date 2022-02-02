package com.qust.assistant.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.ui.fragment.LessonTableFragment;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.UpdateUtil;
import com.qust.assistant.widget.slide.DragType;
import com.qust.assistant.widget.slide.SlidingMenu;

import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity{
	
	private static final int NOT_NOTICE = 2;
	
	private Stack<BaseFragment> fragments;
	
	private SlidingMenu drawer;
	
	private FrameLayout layout;
	
	private Animation animFadeOut;
	
	private Animation animIn, animOut;
	
	private boolean isFading;
	
	@Override
	protected void onCreate(Bundle paramBundle){
		super.onCreate(paramBundle);
		setContentView(R.layout.activity_main);
		
		fragments = new Stack<>();
		
		layout = findViewById(R.id.main_frame);
		
		drawer = findViewById(R.id.main_drawer);
		
		drawer.setDragType(DragType.LEFT);
		
		initHome();
		
		initAnim();
		
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
			ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
		}
		
		UpdateUtil.checkUpdate(this);
	}
	
	private void initAnim(){
		
		animIn = AnimationUtils.loadAnimation(this, R.anim.anim_right_in);
		animIn.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationEnd(Animation param1Animation){ isFading = false; }
			
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
				
				if(fragments.size() > 1){
					fragments.get(fragments.size() - 2).getView().setVisibility(View.VISIBLE);
				}else{
					drawer.setDragType(DragType.LEFT);
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
		BaseFragment home = null;
		
		try{
			Class<?> object = Class.forName(SettingUtil.setting.getString("defaultHome", LessonTableFragment.class.getName()));
			if(BaseFragment.class.isAssignableFrom(object)){
				home = ((BaseFragment)object.newInstance()).init(this, true);
			}else{
				home = new LessonTableFragment().init(this, true);
			}
		}catch(ClassNotFoundException e){
			home = new LessonTableFragment().init(this, true);
		}catch(InstantiationException | IllegalAccessException ignored){ }
		
		fragments.push(home);
		
		layout.addView(home.getView());
	}
	
	public synchronized void addView(Class<? extends BaseFragment> newFragment){
		if(isFading) return;
		try{
			if(fragments.size() > 1) fragments.get(fragments.size() - 2).getView().setVisibility(View.GONE);
			
			BaseFragment a = fragments.peek();
			a.getView().setFocusable(false);
			
			BaseFragment b = ((BaseFragment)newFragment.newInstance()).init(this, false);
			fragments.push(b);
			layout.addView(b.getView());
			
			a.getView().startAnimation(animFadeOut);
			b.getView().startAnimation(animIn);
			
			drawer.setDragType(DragType.RIGHT);
			isFading = true;
		}catch(IllegalAccessException | InstantiationException ignored){ }
	}
	
	public synchronized void removeTopView(){
		if(isFading) return;
		isFading = true;
		fragments.peek().getView().startAnimation(animOut);
	}
	
	@Override
	public void onBackPressed(){
		if(isFading){
			// Do nothing
		}else if(drawer.isMenuShowing()){
			drawer.hideMenu();
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
	
	public void openMenu(){ drawer.openMenu(); }
	
	public void closeMenu(){ drawer.hideMenuNoAnim(); }
	
	public void disableDrag(){
		drawer.disableDrag();;
	}
	
	public void allowDrag(){
		drawer.allowDrag();
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