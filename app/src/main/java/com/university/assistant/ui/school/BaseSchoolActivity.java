package com.university.assistant.ui.school;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.R;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.LoginUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import androidx.annotation.Nullable;

public abstract class BaseSchoolActivity extends BaseAnimActivity{
	
	private String name, password;
	
	private RadioGroup termPicker;
	
	private NumberPicker yearPicker;
	
	protected BaseAdapter adapter;
	
	protected MaterialDialog dialog;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(getLayout());
		
		initToolBar(getName());
		initSliding(null, null);
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		name = data.getString("user",null);
		password = data.getString("password",null);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("查询中...").build();
		
		findViewById(R.id.activity_school_query).setOnClickListener(v -> {
			if(name == null || password == null){
				toast("请先登录！");
				startActivity(new Intent(this,LoginActivity.class));
				finish();
				return;
			}
			new Thread(){
				@Override
				public void run(){
					final String session = LoginUtil.login(name,password);
					if(session==null){
						runOnUiThread(() -> { dialog.dismiss(); toast("登陆失败！用户名或密码错误！"); });
					}else if(session.charAt(0) == '='){
						doQuery(session.substring(1));
					}else{
						runOnUiThread(() -> { dialog.dismiss(); toast(session); });
					}
				}
			}.start();
			dialog.show();
		});
		
	}
	
	protected abstract String getName();
	
	protected abstract int getLayout();
	
	protected abstract void doQuery(String session);
	
	protected void initYearAndTermPicker(){
		yearPicker = findViewById(R.id.activity_school_year);
		yearPicker.setMaxValue(2050);
		yearPicker.setMinValue(2010);
		
		termPicker = findViewById(R.id.activity_school_term);
		
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER){
			yearPicker.setValue(y - 1);
			termPicker.check(R.id.activity_school_term2);
		}else{
			yearPicker.setValue(y);
			termPicker.check(R.id.activity_school_term1);
		}
	}
	
	protected void initList(BaseAdapter _adapter){
		adapter = _adapter;
		ListView listView = findViewById(R.id.activity_school_list);
		listView.setAdapter(adapter);
	}
	
	protected String[] getYearAndTerm(){
		return new String[] {
				String.valueOf(yearPicker.getValue()),
				termPicker.getCheckedRadioButtonId() == R.id.activity_school_term1 ? "3" : "12"
		};
	}
	
	// 载入序列化后的数据
	protected Object loadData(String file, String name){
		try{
			File f = new File(getExternalFilesDir(file),name);
			if(f.exists()){
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				Object o = stream.readObject();
				stream.close();
				return o;
			}else return null;
		}catch(Exception e){
			LogUtil.Log(e);
			return null;
		}
	}
	
	// 储存序列化数据
	protected void saveData(String file, String name, Object o){
		try{
			File f = new File(getExternalFilesDir(file),name);
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
			stream.writeObject(o);
			stream.flush();
			stream.close();
		}catch(IOException e){
			LogUtil.Log(e);
		}
	}
	
}
