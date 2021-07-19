package com.university.assistant.ui.school;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.fragment.lessontable.Lesson;
import com.university.assistant.fragment.lessontable.LessonTableData;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.FileUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.LoginUtil;
import com.university.assistant.util.WebUtil;
import com.university.assistant.widget.LessonTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class GetLessonTableActivity extends BaseActivity{
	
	private MaterialDialog dialog;
	
	private EditText yearText;
	
	private String name, password;
	
	private PagerAdapter adapter;
	
	private ViewPager viewPager;
	
	private Lesson[][] lessons;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_lesson_table);
		
		((Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());
		
		lessons = LessonTableData.getInstance().getLessons();
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("查询中...").build();
		
		name = data.getString("user",null);
		password = data.getString("password",null);
		
		yearText = findViewById(R.id.activity_get_lesson_table_year);
		
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER) y--;
		yearText.setText(String.valueOf(y));
		
		adapter = new LessonAdapter();
		
		viewPager = findViewById(R.id.activity_get_lesson_table_preview);
		viewPager.setAdapter(adapter);
		
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
								adapter.notifyDataSetChanged();
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
				lessons = new Lesson[7][10];
				if(LessonTableData.getInstance().loadFromJson(new JSONObject(response),lessons)){
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
		LessonTableData data = LessonTableData.getInstance();
		data.setLessons(lessons);
		data.saveLessonData();
		sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
		finish();
	}
	
	private class LessonAdapter extends PagerAdapter{
		
		private int total;
		
		private Calendar start;
		
		private SimpleDateFormat sdf;
		
		private String[] week = {"周一","周二","周三","周四","周五","周六","周日"};
		
		public LessonAdapter(){
			total = LessonTableData.getInstance().getTotalWeek();
			sdf = new SimpleDateFormat("MM/dd");
			start = Calendar.getInstance();
			try{
				Date date = new SimpleDateFormat("yyyy/MM/dd").parse(LessonTableData.getInstance().startDay);
				if(date!=null)start.setTime(date);
			}catch(ParseException e){
				LogUtil.Log(e);
			}
		}
		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container,int position){
			ViewGroup layout = (ViewGroup)LayoutInflater.from(GetLessonTableActivity.this).inflate(R.layout.layout_timetable_week,null);
			
			LinearLayout l = layout.findViewById(R.id.layout_timetable_day);
			
			Calendar current = Calendar.getInstance();
			Calendar c = (Calendar)start.clone();
			
			c.add(Calendar.WEEK_OF_YEAR,position);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
			c.add(Calendar.DATE, - dayOfWeek);
			
			for(int i=0;i<7;i++){
				TextView t = new TextView(GetLessonTableActivity.this);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);
				lp.weight = 1;
				t.setLayoutParams(lp);
				t.setPadding(10,20,10,20);
				t.setGravity(Gravity.CENTER);
				t.setText(week[i] + "\n" + sdf.format(c.getTime()));
				t.setTextSize(12);
				if(current.get(Calendar.DATE)==c.get(Calendar.DATE)&&current.get(Calendar.MONTH)==c.get(Calendar.MONTH))
					t.setTextColor(ColorUtil.TEXT_COLORS[1]);
				else t.setTextColor(Color.GRAY);
				l.addView(t);
				c.add(Calendar.DATE,1);
			}
			
			LessonTable lessonTable = layout.findViewById(R.id.layout_timetable_lessons);
			lessonTable.setWeek(position+1);
			lessonTable.setLessons(lessons);
			lessonTable.setLessonClickListener((week, count, lesson) -> {});
			container.addView(layout);
			return layout;
		}
		
		@Override
		public void destroyItem(@NonNull ViewGroup container,int position,@NonNull Object object){ container.removeView((View)object); }
		
		@Override
		public int getCount(){ return total; }
		
		@Override
		public boolean isViewFromObject(@NonNull View view,@NonNull Object object){ return view == object; }
		
		@Override
		public int getItemPosition(@NonNull Object object){
			if ((boolean)((View)object).getTag(R.id.layout_timetable_week)) return POSITION_NONE;
			else return super.getItemPosition(object);
		}
		
		@Override
		public void notifyDataSetChanged(){
			for(int i=0;i<viewPager.getChildCount();i++){
				View child = viewPager.getChildAt(i);
				child.setTag(R.id.layout_timetable_week,true);
			}
			super.notifyDataSetChanged();
		}
	}
	
}
