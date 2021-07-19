package com.university.assistant.ui.school;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.R;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.LoginUtil;
import com.university.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class GetExamActivity extends BaseActivity{
	
	private MaterialDialog dialog;
	
	private EditText yearText;
	
	private String name, password;
	
	private BaseAdapter adapter;
	
	private Exam[] exams;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_exam);
		
		loadData();
		
		((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		name = data.getString("user",null);
		password = data.getString("password",null);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("查询中...").build();
		
		yearText = findViewById(R.id.activity_get_year);
		
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER) y--;
		yearText.setText(String.valueOf(y));
		
		findViewById(R.id.activity_get_query).setOnClickListener(v -> {
			if(name == null || password == null){
				toast("请先登录！");
				startActivity(new Intent(this,LoginActivity.class));
				finish();
				return;
			}
			new Thread(){
				@Override
				public void run(){
					String year = yearText.getText().toString();
					String term = "3";
					if(((RadioGroup)findViewById(R.id.activity_get_term)).getCheckedRadioButtonId()==R.id.activity_get_term2){
						term = "12";
					}
					final String session = LoginUtil.login(name,password);
					if(session==null){
						runOnUiThread(() -> { dialog.dismiss(); toast("登陆失败！用户名或密码错误！"); });
					}else if(session.charAt(0) == '='){
						if(getExam(session.substring(1),year,term)){
							saveData();
							runOnUiThread(() -> {
								dialog.dismiss();
								toast("查询成功！");
							});
						}else{
							runOnUiThread(() -> { dialog.dismiss(); toast("查询失败！"); });
						}
					}else{
						runOnUiThread(() -> { dialog.dismiss(); toast(session); });
					}
				}
			}.start();
			dialog.show();
		});
		
		adapter = new ExamAdapter();
		
		ListView listView = findViewById(R.id.activity_get_exam);
		listView.setAdapter(adapter);
	}
	
	// 载入序列化后的数据
	private void loadData(){
		try{
			File file = new File(getExternalFilesDir("Exam"),"exam");
			
			if(file.exists()){
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
				exams = (Exam[])stream.readObject();
				stream.close();
			}else throw new FileNotFoundException();
			
		}catch(Exception e){
			exams = new Exam[0];
		}
	}
	
	// 储存序列化数据
	private void saveData(){
		try{
			File file = new File(getExternalFilesDir("Exam"),"exam");
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(exams);
			stream.flush();
			stream.close();
		}catch(IOException e){
			LogUtil.Log(e);
		}
	}
	
	private boolean getExam(String session, String year, String term){
		try{
			String response = WebUtil.doPost(
					"http://jwglxt.qust.edu.cn/jwglxt/kwgl/kscx_cxXsksxxIndex.html?doType=query",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50",year,term)
			);
			if(response != null && !"".equals(response)){
				ArrayList<Exam> array = new ArrayList<>();
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i=0;i<item.length();i++){
					JSONObject j = item.getJSONObject(i);
					Exam exam = new Exam();
					exam.name = j.getString("kcmc");
					exam.time = j.getString("kssj");
					exam.place = j.getString("cdmc");
					array.add(exam);
				}
				exams = array.toArray(new Exam[0]);
				runOnUiThread(() -> adapter.notifyDataSetChanged());
				return true;
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private class ExamAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return exams.length; }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(GetExamActivity.this).inflate(R.layout.item_exam,null);
			}
			Exam exam = exams[position];
			((TextView)convertView.findViewById(R.id.item_exam_time)).setText(exam.time);
			((TextView)convertView.findViewById(R.id.item_exam_name)).setText(exam.name);
			((TextView)convertView.findViewById(R.id.item_exam_place)).setText(exam.place);
			return convertView;
		}
	}
	
	private static class Exam implements Serializable{
		
		public String name;
		
		public String place;
		
		public String time;
		
	}
	
}
