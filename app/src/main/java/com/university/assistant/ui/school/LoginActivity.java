package com.university.assistant.ui.school;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.university.assistant.R;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.util.LoginUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends BaseAnimActivity{
	
	private TextInputLayout nameText, passwordText;
	
	private MaterialDialog dialog;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		nameText = findViewById(R.id.activity_login_name);
		passwordText = findViewById(R.id.activity_login_password);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("尝试登陆中...").build();
		
		nameText.getEditText().setText(data.getString("user",""));
		passwordText.getEditText().setText(data.getString("password",""));
		
		findViewById(R.id.activity_login_login).setOnClickListener(v -> {
			new Thread(){
				@Override
				public void run(){
					String user = nameText.getEditText().getText().toString();
					String password = passwordText.getEditText().getText().toString();
					String session = LoginUtil.login(user,password);
					if(session==null){
						runOnUiThread(() -> {
							dialog.dismiss();
							toast("登陆失败！用户名或密码错误！");
						});
					}else if(session.charAt(0) == '='){
						SharedPreferences.Editor editor = data.edit();
						editor.putString("user",user);
						editor.putString("password",password);
						editor.apply();
						runOnUiThread(() -> {
							dialog.dismiss();
							toast("登陆成功！");
						});
					}else{
						runOnUiThread(() -> {
							dialog.dismiss();
							toast(session);
						});
					}

				}
			}.start();
			dialog.show();
		});
		
		initToolBar(null);
		initSliding(null, null);
		
	}
}
