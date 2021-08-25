package com.university.assistant.ui.school;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputLayout;
import com.university.assistant.R;

import androidx.annotation.Nullable;

public class LoginActivity extends BaseSchoolActivity{
	
	private TextInputLayout nameText, passwordText;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		nameText = findViewById(R.id.activity_login_name);
		passwordText = findViewById(R.id.activity_login_password);
		
		nameText.getEditText().setText(data.getString("user",""));
		passwordText.getEditText().setText(data.getString("password",""));
		
		findViewById(R.id.activity_school_query).setOnClickListener(v -> {
			String user = nameText.getEditText().getText().toString();
			String password = passwordText.getEditText().getText().toString();
			new Thread(){
				@Override
				public void run(){
					String errorMsg = loginUtil.login(handler, user, password);
					if(errorMsg == null){
						SharedPreferences.Editor editor = data.edit();
						editor.putString("user", user);
						editor.putString("password", password);
						editor.apply();
						runOnUiThread(() -> {
							dialog.dismiss();
							toast("登陆成功！");
							finish();
						});
					}else{
						runOnUiThread(() -> {
							dialog.dismiss();
							toast(errorMsg);
						});
					}
				}
			}.start();
			dialog.show();
		});
	}
	
	@Override
	protected String getName(){
		return "教务系统登陆";
	}
	
	@Override
	protected int getLayout(){
		return R.layout.activity_login;
	}
	
	@Override
	protected void doQuery(String session){ }
}
