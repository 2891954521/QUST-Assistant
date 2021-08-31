package com.university.assistant.ui.school;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

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
	
	protected String name, password;
	
	protected LoginUtil loginUtil;
	
	private NumberPicker yearPicker;
	
	protected BaseAdapter adapter;
	
	protected MaterialDialog dialog;
	
	protected Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			dialog.setContent((String)msg.obj);
		}
	};
	
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
		
		loginUtil = LoginUtil.getInstance();
		
		findViewById(R.id.activity_school_query).setOnClickListener(v -> {
			if(name == null || password == null){
				toast("请先登录！");
				startActivity(new Intent(this,LoginActivity.class));
				return;
			}
			new Thread(){
				@Override
				public void run(){
					String errorMsg = loginUtil.login(handler, name, password);
					if(errorMsg == null){
						doQuery(loginUtil.JSESSIONID);
					}else{
						runOnUiThread(() -> { dialog.dismiss(); toast(errorMsg); });
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
		
		String[] term = new String[50];
		
		for(int i=0;i<term.length;i++){
			term[i] = (2010 + i / 2) + "-" + (2011 + i / 2) + (i % 2 == 0 ? " 第1学期" : " 第2学期");
		}
		
		yearPicker.setDisplayedValues(term);
		
		yearPicker.setMinValue(0);
		yearPicker.setMaxValue(term.length - 1);
		
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(y < 2010){
			yearPicker.setValue(0);
		}else{
			y = (y - 2010) * 2 - (calendar.get(Calendar.MONTH) < Calendar.AUGUST ? 1 : 0);
			yearPicker.setValue(y < term.length - 1 ? y : term.length - 1);
		}
	}
	
	protected void initList(BaseAdapter _adapter){
		adapter = _adapter;
		ListView listView = findViewById(R.id.activity_school_list);
		listView.setAdapter(adapter);
	}
	
	protected String[] getYearAndTerm(){
		return new String[] {
				String.valueOf(2010 + yearPicker.getValue() / 2),
				yearPicker.getValue() % 2 == 0 ? "3" : "12"
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
