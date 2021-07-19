package com.university.assistant.fragment.lessontable;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.FileUtil;
import com.university.assistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LessonTableData{

	private static LessonTableData data;
	
	public static final int[][] winter = {
			{0,0},{2,10},{3,20},{2,10},{2,20},
	};
	
	public static final int[][] summer = {
			{0,0},{2,10},{3,50},{2,40},{2,50},
	};
	
	private Lesson[][] lessons;
	
	// 开学时间
	public String startDay;
	// 当前周 (从1开始)
	private int currentWeek;
	// 总周数
	private int totalWeek;
	// 星期几 ( 0-6, 周一 —— 周日)
	private int week;
	
	private File dataFile;
	
	private SharedPreferences sp;
	
	private LessonTableData(final Context context){
		
		sp = context.getSharedPreferences("timetable",Context.MODE_PRIVATE);
		
		startDay = sp.getString("startDay","2021/03/08");
		totalWeek = sp.getInt("totalWeek",21);
		
		updateDay();
		
		lessons = new Lesson[7][10];
		
		dataFile = new File(context.getExternalFilesDir("LessonTable"),"data");
		
		if(dataFile.exists()){
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))){
				lessons = (Lesson[][])ois.readObject();
			} catch (Exception e) {
				LogUtil.Log(e);
				try{
					loadFromJson(new JSONObject(FileUtil.readFile(new File(context.getExternalFilesDir("LessonTable"),"data.json"))),lessons);
				}catch(JSONException e1){
					Toast.makeText(context,"载入课表出错！",Toast.LENGTH_SHORT).show();
					LogUtil.Log(e1);
				}finally{
					saveLessonData();
				}
			}
		}else{
			File f = new File(context.getExternalFilesDir("LessonTable"),"data.json");
			if(f.exists()){
				try{
					loadFromJson(new JSONObject(FileUtil.readFile(f)),lessons);
				}catch(JSONException e){
					Toast.makeText(context,"载入课表出错！",Toast.LENGTH_SHORT).show();
					LogUtil.Log(e);
				}finally{
					saveLessonData();
				}
			}
		}
		
	}
	
	public static void init(Context context){
		synchronized(LessonTableData.class){
			if(data==null) data = new LessonTableData(context);
		}
	}
	
	public static LessonTableData getInstance(){
		return data;
	}
	
	public boolean loadFromJson(JSONObject json,Lesson[][] lessons){
		try{
			JSONArray array = json.getJSONArray("kbList");
			
			ArrayList<String> colors = new ArrayList<>();
			int index = 0;
			
			for(int i=0;i<array.length();i++){
				
				JSONObject js = array.getJSONObject(i);
				
				int week = js.getInt("xqj");
				
				String[] sp = js.getString("jcs").split("-");
				
				int count = Integer.parseInt(sp[0]);
				
				Lesson.BaseLesson lesson = Lesson.getLesson(js);
				
				lesson.len = Integer.parseInt(sp[1]) - count + 1;
				
				for(int j=0;j<colors.size();j++){
					if(colors.get(j).equals(lesson.name)){
						lesson.color = j + 1;
						break;
					}
				}
				
				if(lesson.color==0){
					if(++index==ColorUtil.BACKGROUND_COLORS.length) index = 1;
					lesson.color = index;
					colors.add(lesson.name);
				}
				if(lessons[week-1][count-1] == null){
					Lesson l = new Lesson();
					l.week = week;
					l.count = count;
					l.addLesson(lesson);
					lessons[week-1][count-1] = l;
				}else{
					lessons[week-1][count-1].addLesson(lesson);
				}
			}
			return true;
		}catch(JSONException e){
			LogUtil.Log(e);
			return false;
		}
	}
	
	public void saveLessonData(){
		try(ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(dataFile))){
			fos.writeObject(lessons);
			fos.flush();
		}catch(Exception e){
			LogUtil.Log(e);
		}
	}
	
	public void updateDay(){
		Date date = new Date();
		try{
			date = new SimpleDateFormat("yyyy/MM/dd").parse(startDay);
		}catch(ParseException e){ LogUtil.Log(e); }
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		int startWeek = c.get(Calendar.WEEK_OF_YEAR);
		
		c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		
		currentWeek = c.get(Calendar.WEEK_OF_YEAR) - startWeek + 1;
		
		week = c.get(Calendar.DAY_OF_WEEK);
		if(week==Calendar.SUNDAY) week = 6;
		else week -= 2;
	}
	
	public void setStartDay(String _startDay){
		startDay = _startDay;
		sp.edit().putString("startDay",startDay).apply();
	}
	
	public void setTotalWeek(int _totalWeek){
		totalWeek = _totalWeek;
		sp.edit().putInt("totalWeek",totalWeek).apply();
	}
	
	public void setLessons(Lesson[][] _lessons){
		lessons = _lessons;
	}
	
	public Lesson[][] getLessons(){
		return lessons;
	}
	
	public String getStartDay(){
		return startDay==null ? "" : startDay;
	}
	
	public int getCurrentWeek(){
		return currentWeek;
	}
	
	public int getTotalWeek(){
		return totalWeek;
	}
	
	/*
	 * 获取当前是星期几(0-6, 周一 —— 周日)
	 */
	public int getWeek(){
		return week;
	}
}
