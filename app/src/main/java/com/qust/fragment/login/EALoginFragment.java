package com.qust.fragment.login;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputLayout;
import com.qust.account.RequestErrorCallback;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.HandlerCode;
import com.qust.base.fragment.BaseEAFragment;

import java.util.Calendar;

public class EALoginFragment extends BaseEAFragment{
	
	private TextInputLayout nameText;
	
	private TextInputLayout passwordText;
	
	public EALoginFragment(){
		super();
	}
	
	public EALoginFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		nameText = findViewById(R.id.fragment_login_name);
		passwordText = findViewById(R.id.fragment_login_password);
		
		nameText.getEditText().setText(SettingUtil.getString(getString(R.string.EA_NAME), ""));
		passwordText.getEditText().setText(SettingUtil.getString(getString(R.string.EA_PASSWORD), ""));
	}
	
	@Override
	protected void doQuery(){
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
		eaViewModel.loginAsync(user, password, response -> activity.runOnUiThread(() -> {
			// 同步更新入学年份
			String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			int newEntranceTime = Integer.parseInt(year.substring(0, year.length() - 2) + user.substring(0, 2));
			if(entranceTime == 0){
				SettingUtil.edit().putInt(getString(R.string.KEY_ENTRANCE_TIME), newEntranceTime).apply();
			}else if(newEntranceTime != entranceTime){
				DialogUtil.getBaseDialog(activity).content("是否更新入学年份为" + newEntranceTime + "年").onPositive((dialog, what) -> {
					SettingUtil.edit().putInt(getString(R.string.KEY_ENTRANCE_TIME), newEntranceTime).apply();
					dialog.dismiss();
					toast("登陆成功！");
					finish();
				}).show();
				dialog.dismiss();
				return;
			}
			dialog.dismiss();
			toast("登陆成功！");
			finish();
		}), new RequestErrorCallback(){
			@Override
			public void onNeedLogin(){
				sendMessage(HandlerCode.DISMISS_TOAST, "用户名或密码错误");
			}
			
			@Override
			public void onNetworkError(Exception e){
				sendMessage(HandlerCode.DISMISS_TOAST, "网络错误: " + e.getMessage());
			}
		});
	}
	
	@Override
	protected int getLayoutId(){ return R.layout.fragment_login; }
	
	@Override
	public String getName(){
		return "教务系统登陆";
	}
	
}