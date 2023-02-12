package com.qust.assistant.ui.fragment.school;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputLayout;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;

import java.util.Calendar;

public class LoginFragment extends BaseSchoolFragment{
	
	private TextInputLayout nameText;
	
	private TextInputLayout passwordText;
	
	public LoginFragment(){
		super();
	}
	
	public LoginFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		nameText = findViewById(R.id.fragment_login_name);
		passwordText = findViewById(R.id.fragment_login_password);
		
		nameText.getEditText().setText(SettingUtil.getString(SettingUtil.SCHOOL_NAME, ""));
		passwordText.getEditText().setText(SettingUtil.getString(SettingUtil.SCHOOL_PASSWORD, ""));
	}
	
	@Override
	protected void initViewModel(){
		loginViewModel = LoginViewModel.getInstance(activity);
		loginViewModel.getLoginResult().observe(this, result -> {
			if(result.from == handler){
				if(result.cookie != null){
					// 同步更新入学年份
					String user = nameText.getEditText().getText().toString();
					String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					int newEntranceTime = Integer.parseInt(year.substring(0, year.length() - 2) + user.substring(0, 2));
					if(entranceTime == 0){
						SettingUtil.edit().putInt(SettingUtil.KEY_ENTRANCE_TIME, newEntranceTime).apply();
					}else if(newEntranceTime != entranceTime){
						DialogUtil.getBaseDialog(activity).content("是否更新入学年份为" + newEntranceTime + "年").onPositive((dialog, what) -> {
							SettingUtil.edit().putInt(SettingUtil.KEY_ENTRANCE_TIME, newEntranceTime).apply();
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
				}else{
					sendMessage(App.DISMISS_TOAST, result.message);
				}
			}
		});
	}
	
	@Override
	protected void doLogin(){
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
		
		new Thread(() -> loginViewModel.login(handler, user, password)).start();
		dialog.show();
	}
	
	
	@Override
	protected void doQuery(){}
	
	@Override
	protected int getLayoutId(){ return R.layout.fragment_login; }
	
	@Override
	public String getName(){
		return "教务系统登陆";
	}
	
}