package com.qust.assistant.ui.fragment.school;

import android.text.TextUtils;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputLayout;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.SettingUtil;

public class LoginFragment extends BaseSchoolFragment{
	
	private TextInputLayout nameText;
	
	private TextInputLayout passwordText;
	
	public LoginFragment(MainActivity activity){
		super(activity);
	}
	
	public LoginFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		nameText = findViewById(R.id.fragment_login_name);
		passwordText = findViewById(R.id.fragment_login_password);
		
		nameText.getEditText().setText(SettingUtil.getString(SettingUtil.SCHOOL_NAME, ""));
		passwordText.getEditText().setText(SettingUtil.getString(SettingUtil.SCHOOL_PASSWORD, ""));
		
		findViewById(R.id.fragment_school_query).setOnClickListener(v -> {
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
					String errorMsg = LoginViewModel.getInstance(activity).login(handler, user, password);
					if(errorMsg == null){
						SettingUtil.edit().putString(SettingUtil.SCHOOL_NAME, user).putString(SettingUtil.SCHOOL_PASSWORD, password).apply();
						activity.runOnUiThread(() -> {
							dialog.dismiss();
							toast("登陆成功！");
							finish();
						});
					}else{
						sendMessage(App.DISMISS_TOAST, errorMsg);
					}
				}
			}.start();
			dialog.show();
		});
	}
	
	@Override
	protected void doQuery(String JSESSIONID){}
	
	@Override
	protected int getLayoutId(){ return R.layout.fragment_login; }
	
	@Override
	public String getName(){
		return "教务系统登陆";
	}
	
}