package com.qust.assistant.ui.fragment.third;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DrinkFragment extends BaseFragment{
	
	private boolean isRunning;
	
	private boolean isShowingBigCode;
	
	private ImageView arrow;
	
	private ImageView image;
	
	private LinearLayout bottom;
	
	private SeekBar brightnessSeekBar;
	
	private MaterialDialog dialog;
	
	private SharedPreferences sp;
	
	private DrinkViewModel drinkViewModel;
	
	private WindowManager.LayoutParams layoutParams;
	
	public DrinkFragment(MainActivity activity){
		super(activity);
	}
	
	public DrinkFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, false);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		sp = activity.getSharedPreferences("drink", Context.MODE_PRIVATE);
		
		initViews();
		
		initBrightness();
		
		drinkViewModel = DrinkViewModel.getInstance(activity);
		drinkViewModel.getDrinkCodeLiveData().observe(this , code -> createCode());
		drinkViewModel.getLoginSuccessLiveData().observe(this, loginSuccess -> refreshCode());

	}
	
	private void initViews(){
		image = findViewById(R.id.code);
		bottom = findViewById(R.id.bottom);
		arrow = findViewById(R.id.fill_screen_icon);
		
		dialog = new MaterialDialog.Builder(activity).progress(true,0).content("请稍候").canceledOnTouchOutside(false).build();
		
		findViewById(R.id.refresh_code).setOnClickListener(v -> refreshCode());
		findViewById(R.id.drink_login).setOnClickListener(v -> activity.addView(DrinkLoginFragment.class));
		
		findViewById(R.id.fill_screen).setOnClickListener(v -> {
			if(isShowingBigCode) hideCode();
			else showCode();
		});
		
		brightnessSeekBar = findViewById(R.id.main_brightness);
		brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				setBrightness(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar){ }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){
				sp.edit().putInt("drinkBrightness", seekBar.getProgress()).apply();
			}
		});
	}
	
	private void initBrightness(){
		layoutParams = activity.getWindow().getAttributes();
		
		int brightness = sp.getInt("drinkBrightness", (int)layoutParams.screenBrightness);
		
		setBrightness(brightness);
		brightnessSeekBar.setProgress(brightness);
	}
	
	private void setBrightness(int paramInt){
		layoutParams.screenBrightness = paramInt / 255f;
		activity.getWindow().setAttributes(layoutParams);
	}
	
	/**
	 * 联网刷新条形码
	 */
	private void refreshCode(){
		
		if(isRunning) return;
		
		if(drinkViewModel.getUserData().token == null){
			activity.addView(DrinkLoginFragment.class);
			return;
		}
		
		dialog.show();
		new Thread(){
			@Override
			public void run(){
				isRunning = true;
				DrinkData userData = drinkViewModel.getUserData();
				try{
					
					// 验证token有效性
					JSONObject js = new JSONObject(WebUtil.doPost(
							"https://dcxy-customer-app.dcrym.com/app/customer/login",
							null,
							new JSONObject().put("loginTime", String.valueOf(System.currentTimeMillis())).toString(),
							"clientsource", "{}", "token", userData.token, "Content-Type", "application/json"));
					
					if(js.getInt("code") != 1000){
						userData.token = null;
						drinkViewModel.updateUserData(userData);
						activity.runOnUiThread(() -> {
							toast("登陆状态过期，请重新登陆");
							activity.addView(DrinkLoginFragment.class);
							dialog.dismiss();
						});
					}else{
						js = new JSONObject(WebUtil.doGet(
								"https://dcxy-customer-app.dcrym.com/app/customer/flush/idbar",
								null,
								"clientsource", "{}", "token", userData.token));
						
						String data = js.getString("data");
						String code = data.substring(0, data.length() - 1) + "3";
						drinkViewModel.updateDrinkCode(code);
						
						activity.runOnUiThread(() -> dialog.dismiss());
					}
				}catch(JSONException | IOException e){
					activity.runOnUiThread(() -> {
						toast("获取饮水码失败:" + e.getMessage());
						dialog.dismiss();
					});
				}
				isRunning = false;
			}
		}.start();
	}
	
	/**
	 * 创建条形码
	 */
	private void createCode(){
		String code = DrinkData.getDrinkCode(activity);
		if(code == null) return;
		try{
			BitMatrix bitMatrix = new MultiFormatWriter().encode(code, BarcodeFormat.CODE_128, 1000, 200);
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			int[] pixels = new int[width * height];
			for(int y = 0; y < height; y++){
				int offset = y * width;
				for(int x = 0; x < width; x++){
					pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			image.setImageBitmap(bitmap);
		}catch(WriterException ignored){ }
	}
	
	private void hideCode(){
		isShowingBigCode = false;
		
		Animation imageScale = new ScaleAnimation(1f, 1f, 1f, 0.5f);
		imageScale.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		Animation animation2 = new TranslateAnimation(0, 0, 0, - ParamUtil.dp2px(activity, 100));
		animation2.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		Animation arrowAnim = new RotateAnimation(0f, 180f, arrow.getWidth() / 2f, arrow.getHeight() / 2f);
		arrowAnim.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		imageScale.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				arrow.setRotation(-90f);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ParamUtil.dp2px(activity, 100));
				image.setLayoutParams(layoutParams);
				bottom.setPadding(0, ParamUtil.dp2px(activity, 100), 0, 0);
				
				arrow.clearAnimation();
				image.clearAnimation();
				bottom.clearAnimation();
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
		image.startAnimation(imageScale);
		arrow.startAnimation(arrowAnim);
		bottom.startAnimation(animation2);
	}
	
	private void showCode(){
		isShowingBigCode = true;
		
		Animation imageScale = new ScaleAnimation(1f, 1f, 1f, 2f);
		imageScale.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		Animation bottomAnim = new TranslateAnimation(0, 0, 0, ParamUtil.dp2px(activity, 100));
		bottomAnim.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		Animation arrowAnim = new RotateAnimation(0f, 180f, arrow.getWidth() / 2f, arrow.getHeight() / 2f);
		arrowAnim.setDuration(getResources().getInteger(R.integer.anim_speed));
		
		imageScale.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				arrow.setRotation(90f);
				
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ParamUtil.dp2px(activity, 200));
				image.setLayoutParams(layoutParams);
				bottom.setPadding(0, ParamUtil.dp2px(activity, 200), 0, 0);
				
				arrow.clearAnimation();
				image.clearAnimation();
				bottom.clearAnimation();
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
		
		image.startAnimation(imageScale);
		arrow.startAnimation(arrowAnim);
		bottom.startAnimation(bottomAnim);
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_drink;
	}
	
	@Override
	public String getName(){
		return "饮水码";
	}
	
}