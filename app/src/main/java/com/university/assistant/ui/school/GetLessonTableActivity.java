package com.university.assistant.ui.school;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.App;
import com.university.assistant.Lesson.LessonData;
import com.university.assistant.Lesson.LessonGroup;
import com.university.assistant.R;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.util.FileUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.LoginUtil;
import com.university.assistant.util.WebUtil;
import com.university.assistant.widget.LessonTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class GetLessonTableActivity extends BaseActivity{
	
	private MaterialDialog dialog;
	
	private EditText yearText;
	
	private String name, password;
	
	private LessonTable lessonTable;
	
	private LessonGroup[][] lessonGroups;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_lesson_table);
		
		((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());
		
		lessonGroups = new LessonGroup[7][10];
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("查询中...").build();
		
		name = data.getString("user",null);
		password = data.getString("password",null);
		
		yearText = findViewById(R.id.activity_get_lesson_table_year);
		
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER) y--;
		yearText.setText(String.valueOf(y));
		
		lessonTable = findViewById(R.id.activity_get_lesson_table_preview);
		lessonTable.initAdapter(lessonGroups);
		lessonTable.setLessonClickListener((week, count, lesson) -> { });
		lessonTable.setUpdateListener(() -> {
			int currentWeek = lessonTable.getCurrentItem();
			lessonTable.setAdapter(lessonTable.getAdapter());
			lessonTable.setCurrentItem(currentWeek);
		});

		lessonTable.setCurrentItem(LessonData.getInstance().getCurrentWeek() - 1);
		
		findViewById(R.id.activity_get_lesson_table_done).setOnClickListener(v -> updateLesson());
		
		findViewById(R.id.activity_get_lesson_table_query).setOnClickListener(v -> {
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
					if(((RadioGroup)findViewById(R.id.activity_get_lesson_table_term)).getCheckedRadioButtonId()==R.id.activity_get_lesson_table_term2){
						term = "12";
					}
					final String session = LoginUtil.login(name,password);
					if(session==null){
						runOnUiThread(() -> { dialog.dismiss(); toast("登陆失败！用户名或密码错误！"); });
					}else if(session.charAt(0) == '='){
						if(getLessonTable(session.substring(1),year,term)){
							runOnUiThread(() -> {
								lessonTable.initAdapter(lessonGroups);
								dialog.dismiss();
								toast("获取课表成功！");
							});
						}else{
							runOnUiThread(() -> { dialog.dismiss(); toast("获取课表失败！"); });
						}
					}else{
						runOnUiThread(() -> { dialog.dismiss(); toast(session); });
					}
				}
			}.start();
			dialog.show();
		});
	}
	
	// 获取课表
	private boolean getLessonTable(String session, String year, String term){
		try{
			String response = WebUtil.doPost(
					"http://jwglxt.qust.edu.cn/jwglxt/kbcx/xskbcx_cxXsKb.html",
					"JSESSIONID=" + session ,
					"xnm=" + year +"&xqm=" + term + "&kzlx=ck"
			);
			if(response != null && !"".equals(response)){
				lessonGroups = new LessonGroup[7][10];
				if(LessonData.getInstance().loadFromJson(new JSONObject(response),lessonGroups)){
					FileUtil.writeFile(new File(getExternalFilesDir("LessonTable"),"data.json"),response);
					return true;
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private void updateLesson(){
		LessonData data = LessonData.getInstance();
		data.setLessonGroups(lessonGroups);
		data.saveLessonData();
		sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
		finish();
	}
	
}
