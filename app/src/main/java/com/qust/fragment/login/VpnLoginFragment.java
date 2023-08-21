package com.qust.fragment.login;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.qust.account.RequestErrorCallback;
import com.qust.account.vpn.VpnViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.fragment.BaseFragment;

public class VpnLoginFragment extends BaseFragment{
	
	private VpnViewModel vpnViewModel;
	
	private TextInputLayout nameText;
	private TextInputLayout passwordText;
	
	private MaterialDialog dialog;
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		vpnViewModel = VpnViewModel.getInstance(activity);
		
		dialog = DialogUtil.getIndeterminateProgressDialog(activity, "登录中").build();
		
		
		nameText = findViewById(R.id.fragment_login_name);
		passwordText = findViewById(R.id.fragment_login_password);
		
		nameText.getEditText().setText(SettingUtil.getString(getString(R.string.VPN_NAME), ""));
		passwordText.getEditText().setText(SettingUtil.getString(getString(R.string.VPN_PASSWORD), ""));
		
		findViewById(R.id.fragment_school_query).setOnClickListener(this::login);
		
	}
	
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
		
		vpnViewModel.loginAsync(user, password, (response, html) -> activity.runOnUiThread(() -> {
			toast("登录成功");
			dialog.dismiss();
			finish();
		}), new RequestErrorCallback(){
			@Override
			public void onNetworkError(Exception e){
				activity.runOnUiThread(() -> {
					toast("网络错误: " + e.getMessage());
					dialog.dismiss();
				});
			}
			@Override
			public void onNeedLogin(){
				activity.runOnUiThread(() -> {
					toast("账号或密码错误");
					dialog.dismiss();
				});
			}
		});
		dialog.show();
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_login;
	}
	
	@Override
	public String getName(){
		return "智慧青科大登录";
	}
}
