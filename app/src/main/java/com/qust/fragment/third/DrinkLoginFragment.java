package com.qust.fragment.third;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.qust.assistant.R;
import com.qust.assistant.util.WebUtil;
import com.qust.base.fragment.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DrinkLoginFragment extends BaseFragment{
	
	private boolean isRunning;
	
	private MaterialDialog dialog;
	
	private TextInputLayout phone;
	
	private TextInputLayout password;
	
	private DrinkViewModel drinkViewModel;
	
	public DrinkLoginFragment(){
		super();
	}
	
	public DrinkLoginFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		phone = findViewById(R.id.login_phone);
		password = findViewById(R.id.login_password);
		
		findViewById(R.id.login_login).setOnClickListener(v -> login());
		
		drinkViewModel = DrinkViewModel.getInstance(activity);
		drinkViewModel.getUserLiveData().observe(this, userData -> {
			phone.getEditText().setText(userData.phone);
			password.getEditText().setText(userData.password);
		});
		
		dialog = new MaterialDialog.Builder(activity)
				.progress(true,0)
				.content("请稍候")
				.canceledOnTouchOutside(false)
				.build();
	}
	
	private void login(){
		
		if(isRunning) return;
		
		if(TextUtils.isEmpty(phone.getEditText().getText())){
			phone.setError("请输入手机号码");
			return;
		}else{
			phone.setError(null);
		}
		
		if(TextUtils.isEmpty(password.getEditText().getText())){
			password.setError("请输入密码");
			return;
		}else{
			password.setError(null);
		}
		
		dialog.show();
		
		new Thread(){
			@Override
			public void run(){
				isRunning = true;
				try{
					DrinkData data = drinkViewModel.getUserData();
					data.phone = phone.getEditText().getText().toString();
					data.password = password.getEditText().getText().toString();
					
					JSONObject js = new JSONObject(WebUtil.doPost(
							"https://dcxy-customer-app.dcrym.com/app/customer/login",
							null,
							new JSONObject().put("loginAccount", data.phone).put("password", data.password).toString(),
							"clientsource", "{}", "Content-Type", "application/json"));
					
					if(js.getInt("code") == 1000){
						data.token = js.getJSONObject("data").getString("token");
						drinkViewModel.login(data);
						activity.runOnUiThread(() -> {
							toast("登录成功");
							dialog.dismiss();
							finish();
						});
					}else{
						String msg = js.getString("msg");
						activity.runOnUiThread(() -> {
							toast("登陆失败: " + msg);
							dialog.dismiss();
						});
					}
					
				}catch(JSONException | IOException e){
					activity.runOnUiThread(() -> {
						toast("登陆失败");
						dialog.dismiss();
					});
				}
				isRunning = false;
			}
		}.start();
	}

	@Override
	protected int getLayoutId(){
		return R.layout.fragment_drink_login;
	}
	
	@Override
	public String getName(){
		return "登录";
	}
}
