package com.qust.assistant.lesson;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.preference.PreferenceManager;

/**
 * 储存所有课程信息的类
 */
public class LessonData{

	private static LessonData data;
	
	// 时间
	public static final String[][][] LESSON_TIME_TEXT = new String[][][]{
		{
			{"08:00", "09:00", "10:10", "11:10", "13:30", "14:30", "15:40", "16:40", "18:00", "19:00"},
			{"08:50", "09:50", "11:00", "12:00", "14:20", "15:20", "16:30", "17:30", "18:50", "19:50"}
		},{
			{"08:00", "09:00", "10:10", "11:10", "14:00", "15:00", "16:10", "17:10", "18:30", "19:30"},
			{"08:50", "09:50", "11:00", "12:00", "14:50", "15:50", "17:00", "18:00", "19:20", "20:20"}
		},
	};
	
	// 时间差 (单位：分钟)
	public static final int[][] LESSON_TIME = {
		// 冬季
		{ 0, 60, 70, 60, 140, 60, 70, 60, 80, 60 },
		// 夏季
		{ 0, 60, 70, 60, 170, 60, 70, 60, 80, 60 }
	};
	
	public static int[] LessonTime;
	
	/**
	 * 上下课时间文本
	 */
	public static String[][] LessonTimeText;
	
	/**
	 * 所有课程
	 */
	private LessonGroup[][] lessonGroups;
	
	/**
	 * 开学时间
 	 */
	private String startDay;
	
	/**
	 * 当前周 (从1开始)
	 */
	private int currentWeek;
	
	/**
	 * 总周数
 	 */
	private int totalWeek;
	
	/**
	 * 当前星期几 ( 0-6, 周一 —— 周日)
 	 */
	private int week;
	
	// 序列化后的课表
	private File dataFile;
	
	private SharedPreferences sp;
	
	private LessonData(final Context context){
		
		sp = context.getSharedPreferences("timetable", Context.MODE_PRIVATE);
		
		startDay = sp.getString("startDay","2022-02-28");
		totalWeek = sp.getInt("totalWeek",21);
		
		updateDate();
		
		lessonGroups = new LessonGroup[7][10];
		
		initLessonTime(context);
		
		initLesson(context);
	}
	
	public static void init(Context context){
		synchronized(LessonData.class){
			if(data == null) data = new LessonData(context);
		}
	}
	
	public static LessonData getInstance(){ return data; }
	
	// 初始化课程时间
	public void initLessonTime(Context context){
		int time = PreferenceManager.getDefaultSharedPreferences(context).getInt("lessonTime", 0);
		LessonTime = LESSON_TIME[time];
		LessonTimeText = LESSON_TIME_TEXT[time];
	}
	
	// 初始化课表
	private void initLesson(Context context){
		dataFile = new File(context.getExternalFilesDir("LessonTable"),"data");
		if(dataFile.exists()){
			// 从序列化后的数据中读取课表
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))){
				lessonGroups = (LessonGroup[][]) ois.readObject();
				return;
			}catch(Exception e){
				Toast.makeText(context, "载入序列化课表出错！", Toast.LENGTH_SHORT).show();
				LogUtil.Log(e);
			}
		}
		
		try{
			// 从文件中读取课表
			lessonGroups = new LessonGroup[7][10];
			loadFromJson(new JSONObject(FileUtil.readFile(new File(context.getExternalFilesDir("LessonTable"), "data.json"))), lessonGroups);
		}catch(JSONException e){
			Toast.makeText(context, "载入课表出错！", Toast.LENGTH_SHORT).show();
			LogUtil.Log(e);
		}finally{
			saveLessonData();
		}
	}
	
	// 从json中解析课表
	public boolean loadFromJson(JSONObject json, LessonGroup[][] lessonGroups){
		try{
			if(!json.has("kbList")) return false;
			
			JSONArray array = json.getJSONArray("kbList");
			
			ArrayList<String> colors = new ArrayList<>();
			int index = 0;
			
			for(int i = 0; i < array.length(); i++){
				
				JSONObject js = array.getJSONObject(i);
				
				int week = js.getInt("xqj");
				
				String[] sp = js.getString("jcs").split("-");
				
				int count = Integer.parseInt(sp[0]);
				
				Lesson lesson = new Lesson(js);
				
				lesson.len = Integer.parseInt(sp[1]) - count + 1;
				
				for(int j = 0; j < colors.size(); j++){
					if(colors.get(j).equals(lesson.name)){
						lesson.color = j % (ColorUtil.BACKGROUND_COLORS.length - 1) + 1;
						break;
					}
				}
				
				if(lesson.color == 0){
					if(++index == ColorUtil.BACKGROUND_COLORS.length) index = 1;
					lesson.color = index;
					colors.add(lesson.name);
				}
				
				if(lessonGroups[week - 1][count - 1] == null){
					lessonGroups[week - 1][count - 1] = new LessonGroup(week, count);
				}
				lessonGroups[week - 1][count - 1].addLesson(lesson);
			}
			return true;
		}catch(JSONException e){
			LogUtil.Log(e);
			return false;
		}
	}
	
	// 序列化储存课表数据
	public void saveLessonData(){
		// 去除上课周数为0的课程
		for(LessonGroup[] value : lessonGroups){
			for(LessonGroup lessonGroup : value){
				if(lessonGroup==null || lessonGroup.lessons.length<2) continue;
				for(int k = 0;k<lessonGroup.lessons.length;k++){
					boolean flag = true;
					for(int m = 0;m<lessonGroup.lessons[k].week.length;m++){
						if(lessonGroup.lessons[k].week[m]){
							flag = false;
							break;
						}
					}
					if(flag){
						lessonGroup.removeLesson(k);
						break;
					}
				}
			}
		}
		try(ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(dataFile))){
			fos.writeObject(lessonGroups);
			fos.flush();
		}catch(Exception e){
			LogUtil.Log(e);
		}
	}
	
	/**
	 * 更新日期
 	 */
	public void updateDate(){
		Date date;
		
		try{
			date = DateUtil.YMD.parse(startDay);
		}catch(ParseException e){
			date = new Date();
		}
		
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTime(date);
		
		int startWeek = c.get(Calendar.WEEK_OF_YEAR);
		
		c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		c.setFirstDayOfWeek(Calendar.MONDAY);
		
		currentWeek = Math.max(1, c.get(Calendar.WEEK_OF_YEAR) - startWeek + 1);
		
		week = c.get(Calendar.DAY_OF_WEEK);
		if(week == Calendar.SUNDAY) week = 6;
		else week -= 2;
	}
	
	// 检查课程是否冲突
	public boolean isConflict(int week,int count,Lesson lesson,int len,boolean[] booleans){
		for(int i = count;i<count+len&&i<lessonGroups[0].length;i++){
			LessonGroup l = lessonGroups[week][i];
			if(l == null) continue;
			for(int j=0;j<l.lessons.length;j++){
				// 忽略自己
				if(l.lessons[j].equals(lesson)) continue;
				for(int b=0;b<l.lessons[j].week.length;b++){
					if(l.lessons[j].week[b] && booleans[b]){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void setStartDay(String _startDay){
		startDay = _startDay;
		updateDate();
		sp.edit().putString("startDay", startDay).apply();
	}
	
	public void setTotalWeek(int _totalWeek){
		totalWeek = _totalWeek;
		sp.edit().putInt("totalWeek",totalWeek).apply();
	}
	
	public void setLessonGroups(LessonGroup[][] _lessonGroups){
		lessonGroups = _lessonGroups;
	}
	
	public LessonGroup[][] getLessonGroups(){
		return lessonGroups;
	}
	
	/**
	 * 获取开学时间 yyyy-MM-dd
	 */
	public String getStartDay(){
		return startDay == null ? "" : startDay;
	}
	
	/**
	 * 获取当前周（从1开始）
	 */
	public int getCurrentWeek(){
		return currentWeek;
	}
	
	/**
	 * 获取学期总周数
	 */
	public int getTotalWeek(){
		return totalWeek;
	}
	
	/**
	 * 获取当前是星期几(0-6, 周一 —— 周日)
	 */
	public int getWeek(){
		return week;
	}
}
