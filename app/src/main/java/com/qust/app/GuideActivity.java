package com.qust.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputLayout;
import com.qust.account.RequestErrorCallback;
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.lesson.LessonTableModel;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.QueryLessonResult;
import com.qust.base.HandlerCode;
import com.qust.base.ui.BaseActivity;

import java.util.Calendar;

/**
 * 首次使用的引导页
 */
public class GuideActivity extends BaseActivity{
	
	private LinearLayout linearLayout;
	
	private TextInputLayout nameText, passwordText;
	
	private LottieAnimationView lottieView;
	
	private MaterialDialog dialog;
	
	private int currentYear, entranceTime;
	
	private EAViewModel eaViewModel;
	
	protected Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case HandlerCode.UPDATE_DIALOG:
					dialog.setContent((String)msg.obj);
					break;
				case HandlerCode.DISMISS_TOAST:
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
		linearLayout = findViewById(R.id.linearLayout);
		
		findViewById(R.id.skip).setOnClickListener(v -> {
			SettingUtil.edit().putBoolean(getString(R.string.isFirstUse), false).apply();
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
		
		findViewById(R.id.done).setOnClickListener(this::login);
		
		eaViewModel = EAViewModel.getInstance(this);

		initAnim();
	}
	
	/**
	 * 启动动画
	 */
	private void initAnim(){
		lottieView = findViewById(R.id.lottieView);
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
	private void login(View v){
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
		
		dialog.show();
		
		String year = String.valueOf(currentYear);
		entranceTime = Integer.parseInt(year.substring(0, year.length() - 2) + user.substring(0, 2));
		
		eaViewModel.loginAsync(user, password, response -> getLessonTable(), new RequestErrorCallback(){
			@Override
			public void onNeedLogin(){
				handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, "用户名或密码错误"));
			}
			@Override
			public void onNetworkError(Exception e){
				handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, "网络错误: " + e.getMessage()));
			}
		});
	}
	
	private void getLessonTable(){
		SettingUtil.edit().putInt(getString(R.string.KEY_ENTRANCE_TIME), entranceTime).apply();
		
		handler.sendMessage(handler.obtainMessage(HandlerCode.UPDATE_DIALOG, "正在查询课表"));
		
		int index = LessonTableModel.getCurrentYear(entranceTime);
		
		QueryLessonResult result = LessonTableModel.queryLessonTable(eaViewModel, String.valueOf(index / 2 + entranceTime), entranceTime % 2 == 0 ? "3" : "12");
		
		LessonTableViewModel.getInstance(this).saveLessonData(result.lessonTable);
		
		runOnUiThread(() -> {
			SettingUtil.edit().putBoolean(getString(R.string.isFirstUse), false).apply();
			dialog.dismiss();
			toastOK("初始化完成");
			startActivity(new Intent(GuideActivity.this, MainActivity.class));
			finish();
		});
	}
}
