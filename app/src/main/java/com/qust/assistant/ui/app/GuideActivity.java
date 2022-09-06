package com.qust.assistant.ui.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputLayout;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.ui.BaseActivity;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.QustUtil.LessonUtil;
import com.qust.assistant.util.SettingUtil;

import java.util.Calendar;

/**
 * 首次使用的引导页
 */
public class GuideActivity extends BaseActivity{
	
	private static final int NOT_NOTICE = 2;
	
	private LinearLayout linearLayout;
	
	private TextInputLayout nameText;
	
	private TextInputLayout passwordText;
	
	private LottieAnimationView lottieView;
	
	private MaterialDialog dialog;
	
	private int currentYear;
	
	private int entranceTime;
	
	protected Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case App.UPDATE_DIALOG:
					dialog.setContent((String)msg.obj);
					break;
				case App.DISMISS_TOAST:
					dialog.dismiss();
					toast((String)msg.obj);
					break;
			}
		}
	};
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		
		currentYear = Calendar.getInstance().get(Calendar.YEAR);
		
		dialog = DialogUtil.getIndeterminateProgressDialog(this, "登录中").build();
		
		nameText = findViewById(R.id.input_name);
		passwordText = findViewById(R.id.input_password);
		lottieView = findViewById(R.id.lottieView);
		linearLayout = findViewById(R.id.linearLayout);
		
		nameText.getEditText().addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}

			@Override
			public void afterTextChanged(Editable s){
				if(s.length() > 2){
					try{
						String year = String.valueOf(currentYear);
						entranceTime = Integer.parseInt(year.substring(0, year.length() - 2) + s.toString().substring(0, 2));
//						if(y >= numberPicker.getMinValue() && y <= numberPicker.getMaxValue()){
//							numberPicker.setValue(y);
//						}
					}catch(NumberFormatException | IndexOutOfBoundsException ignored){}
				}
			}
		});
		
		findViewById(R.id.skip).setOnClickListener(v -> {
			SettingUtil.edit().putBoolean(SettingUtil.IS_FIRST_USE, false).apply();
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
		
		findViewById(R.id.done).setOnClickListener(v -> {
			// 先请求权限
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
				ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
			}else{
				loginAndGetLessonTable();
			}
		});

		lottieView.setSpeed(1.25f);
		lottieView.addAnimatorListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){
				lottieView.removeAnimatorListener(this);
				lottieView.setVisibility(View.GONE);
				
				Animation anim = AnimationUtils.loadAnimation(GuideActivity.this, R.anim.anim_fade_out);
				anim.setAnimationListener(new Animation.AnimationListener(){
					@Override
					public void onAnimationStart(Animation animation){ }
					@Override
					public void onAnimationEnd(Animation animation){
						lottieView.setVisibility(View.GONE);
						linearLayout.setVisibility(View.VISIBLE);
					}
					@Override
					public void onAnimationRepeat(Animation animation){ }
				});
				lottieView.startAnimation(anim);
				linearLayout.startAnimation(AnimationUtils.loadAnimation(GuideActivity.this, R.anim.anim_flow_in));
			}
		});
	}
	
	/**
	 * 教务登录并获取课表
	 */
	private void loginAndGetLessonTable(){
		String user = nameText.getEditText().getText().toString();
		if(TextUtils.isEmpty(user)){
			nameText.setError("请输入学号");
			return;
		}else{
			nameText.setError(null);
		}
		
		String password = passwordText.getEditText().getText().toString();
		if(TextUtils.isEmpty(password)){
			passwordText.setError("请输入密码");
			return;
		}else{
			passwordText.setError(null);
		}
		
		new Thread(){
			@Override
			public void run(){
				LoginViewModel loginViewModel = LoginViewModel.getInstance(GuideActivity.this);
				String errorMsg = loginViewModel.login(handler, user, password);
				if(errorMsg != null){
					handler.sendMessage(handler.obtainMessage(App.DISMISS_TOAST, errorMsg));
					return;
				}
				
				SettingUtil.edit()
						.putString(SettingUtil.SCHOOL_NAME, user)
						.putString(SettingUtil.SCHOOL_PASSWORD, password)
						.putInt(SettingUtil.KEY_ENTRANCE_TIME, entranceTime)
						.apply();
				
				handler.sendMessage(handler.obtainMessage(App.UPDATE_DIALOG, "正在查询课表"));
				
				int index = LessonUtil.getCurrentYear(entranceTime);
				
				LessonUtil.QueryLessonResult result = LessonUtil.queryLessonTable(loginViewModel, loginViewModel.getCookie(),
						String.valueOf(index / 2 + entranceTime),
						entranceTime % 2 == 0 ? "3" : "12");
				
				LessonTableViewModel lessonTableViewModel = LessonTableViewModel.getInstance(GuideActivity.this);
				lessonTableViewModel.saveLessonData(result.startTime, result.totalWeek, result.lessonGroups);
				
				runOnUiThread(() -> {
					SettingUtil.edit().putBoolean(SettingUtil.IS_FIRST_USE, false).apply();
					dialog.dismiss();
					toast("初始化完成");
					startActivity(new Intent(GuideActivity.this, MainActivity.class));
					finish();
				});
			}
		}.start();
		dialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == NOT_NOTICE){
			// 由于不知道是否选择了允许所以需要再次判断
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
			}else{
				loginAndGetLessonTable();
			}
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
									System.exit(0);
								}).negativeText(R.string.text_ok).onNegative((dialog, which) -> {
									Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
									// 注意就是"package",不用改成自己的包名
									Uri uri = Uri.fromParts("package", getPackageName(), null);
									intent.setData(uri);
									startActivityForResult(intent, NOT_NOTICE);
									dialog.dismiss();
								}).show();
					}else{
						// 选择禁止
						new MaterialDialog.Builder(this).title("权限").content("请给予应用运行所必要的权限！")
								.positiveText(R.string.text_cancel).onPositive((dialog, which) -> {
									toast("请给予应用运行所必要的权限！");
									System.exit(0);
								}).negativeText(R.string.text_ok).onNegative((dialog, which) -> {
									ActivityCompat.requestPermissions(GuideActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
									dialog.dismiss();
								}).show();
					}
					break;
				}
			}
		}
	}
}
