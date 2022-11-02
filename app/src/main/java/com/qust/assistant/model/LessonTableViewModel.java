package com.qust.assistant.model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.App;
import com.qust.assistant.model.lesson.Lesson;
import com.qust.assistant.model.lesson.LessonGroup;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

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

public class LessonTableViewModel extends AndroidViewModel{
	
	/**
	 * 课程时间表
 	 */
	public static final String[][][] LESSON_TIME_TEXT = new String[][][]{
		{
			{"08:00", "09:00", "10:10", "11:10", "13:30", "14:30", "15:40", "16:40", "18:00", "19:00"},
			{"08:50", "09:50", "11:00", "12:00", "14:20", "15:20", "16:30", "17:30", "18:50", "19:50"}
		},{
			{"08:00", "09:00", "10:10", "11:10", "14:00", "15:00", "16:10", "17:10", "18:30", "19:30"},
			{"08:50", "09:50", "11:00", "12:00", "14:50", "15:50", "17:00", "18:00", "19:20", "20:20"}
		}
	};
	
	/**
	 * 课程时间差 (单位：分钟)
	 */
	public static final int[][] LESSON_TIME = {
		// 冬季
		{ 0, 60, 70, 60, 140, 60, 70, 60, 80, 60 },
		// 夏季
		{ 0, 60, 70, 60, 170, 60, 70, 60, 80, 60 }
	};
	
	
	/**
	 * 课程时间差
	 */
	private static int[] lessonTime;
	
	/**
	 * 上下课时间文本
	 */
	private static String[][] lessonTimeText;
	
	/**
	 * 开学时间
	 */
	private static String startDay;
	
	/**
	 * 总周数
	 */
	private static int totalWeek;
	
	/**
	 * 当前周 (从1开始)
	 */
	private static int currentWeek;
	
	/**
	 * 当前星期 ( 0-6, 周一 —— 周日)
	 */
	private static int dayOfWeek;
	
	/**
	 * 所有课程
	 */
	private static LessonGroup[][] lessonGroups;
	
	/**
	 * 是否需要更新课表UI（课表信息变化时的回调）
	 */
	private final MutableLiveData<Boolean> needUpdateLesson;
	
	public static LessonTableViewModel getInstance(@NonNull Context context){
		return ((App)context.getApplicationContext()).lessonTableViewModel;
	}
	
	public LessonTableViewModel(@NonNull Application application){
		super(application);
		needUpdateLesson = new MutableLiveData<>(false);
		
		lessonGroups = new LessonGroup[7][10];
		
		startDay = SettingUtil.getString(SettingUtil.KEY_START_DAY,"2022-08-29");
		totalWeek = SettingUtil.getInt(SettingUtil.KEY_TOTAL_WEEK,21);
		
		dayOfWeek = 0;
		currentWeek = 1;
		
		// 初始化课程时间表
		int time = SettingUtil.getInt(SettingUtil.KEY_TIME_TABLE, 0);
		lessonTime = LESSON_TIME[time];
		lessonTimeText = LESSON_TIME_TEXT[time];
		
		// 更新时间
		updateDate();
		
		initLesson(application);
	}
	
	/**
	 *
	 */
	public MutableLiveData<Boolean> getUpdateLiveData(){
		return needUpdateLesson;
	}
	
	/**
	 * 从本地文件初始化课表
	 */
	private void initLesson(@NonNull Context context){
		File dataFile = new File(context.getExternalFilesDir("LessonTable"),"data");
		if(dataFile.exists()){
			// 从序列化后的数据中读取课表
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))){
				lessonGroups = (LessonGroup[][]) ois.readObject();
				return;
			}catch(Exception e){
				LogUtil.Log(e);
			}
		}
		
		File jsonFile = new File(context.getExternalFilesDir("LessonTable"), "data.json");
		if(jsonFile.exists()){
			try{
				// 从文件中读取课表
				if(loadFromJson(new JSONObject(FileUtil.readFile(jsonFile)), lessonGroups)){
					saveLessonData(context, lessonGroups);
				}
			}catch(JSONException e){
				LogUtil.Log(e);
				lessonGroups = new LessonGroup[7][10];
			}
		}
	}
	
	
	/**
	 * 更新日期信息
	 */
	public void updateDate(){
		Date date;
		
		try{
			date = DateUtil.YMD.parse(startDay);
		}catch(ParseException | NullPointerException e){
			date = new Date();
		}
		
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTime(date);
		
		int startWeek = c.get(Calendar.WEEK_OF_YEAR);
		
		c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		c.setFirstDayOfWeek(Calendar.MONDAY);
		
		currentWeek = Math.max(1, c.get(Calendar.WEEK_OF_YEAR) - startWeek + 1);
		
		int day = c.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = (day == Calendar.SUNDAY ? 6 : day - 2);
	}
	
	
	/**
	 * 设置开学时间
	 */
	public void setStartDay(@NonNull String _startDay){
		startDay = _startDay;
		updateDate();
		SettingUtil.edit().putString(SettingUtil.KEY_START_DAY, _startDay).apply();
		needUpdateLesson.postValue(true);
	}
	
	/**
	 * 设置学期总周数
	 */
	public void setTotalWeek(int _totalWeek){
		totalWeek = _totalWeek;
		SettingUtil.edit().putInt(SettingUtil.KEY_TOTAL_WEEK, _totalWeek).apply();
		needUpdateLesson.postValue(true);
	}
	
	/**
	 * 设置课表
	 */
	public void setLessonGroups(LessonGroup[][] _lessonGroups){
		lessonGroups = _lessonGroups;
		needUpdateLesson.postValue(true);
	}

	/**
	 * 设置课程时间表
	 */
	public void setTimeTable(int index){
		lessonTime = LESSON_TIME[index];
		lessonTimeText = LESSON_TIME_TEXT[index];
		needUpdateLesson.postValue(true);
	}
	
	/**
	 * 设置并保存课程表信息
	 * @param _startDay 开学时间
	 * @param _totalWeek 学期总周数
	 * @param _lessonGroups 课表
	 */
	public void saveLessonData(@Nullable String _startDay, int _totalWeek, @NonNull LessonGroup[][] _lessonGroups){
		
		if(_startDay != null){
			startDay = _startDay;
			updateDate();
			SettingUtil.edit().putString(SettingUtil.KEY_START_DAY, _startDay).apply();
		}
		
		if(_totalWeek > 1){
			totalWeek = _totalWeek;
			SettingUtil.edit().putInt(SettingUtil.KEY_TOTAL_WEEK, _totalWeek).apply();
		}
		
		lessonGroups = _lessonGroups;
		
		saveLessonData(getApplication(), lessonGroups);
		
		needUpdateLesson.postValue(true);
	}
	
	
	public static LessonGroup[][] getLessonGroups(){
		return lessonGroups;
	}
	
	/**
	 * 获取开学时间 yyyy-MM-dd
	 */
	public static String getStartDay(){
		return startDay;
	}
	
	/**
	 * 获取学期总周数
	 */
	public static int getTotalWeek(){
		return totalWeek;
	}
	
	/**
	 * 获取当前周（从1开始）
	 */
	public static int getCurrentWeek(){
		return currentWeek;
	}
	
	/**
	 * 获取当前是星期几(0-6, 周一 —— 周日)
	 */
	public static int getDayOfWeek(){
		return dayOfWeek;
	}
	
	/**
	 * 获取课程时间差
	 */
	public static int[] getLessonTime(){
		return lessonTime;
	}
	
	/**
	 * 获取课程时间文本
	 */
	public static String[][] getLessonTimeText(){
		return lessonTimeText;
	}
	
	/**
	 * 从json中解析课表
 	 */
	public static boolean loadFromJson(@NonNull JSONObject json, LessonGroup[][] lessonGroups){
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
	
	
	/**
	 * 序列化储存课表数据
	 */
	public static void saveLessonData(Context context, @NonNull LessonGroup[][] lessonGroups){
		// 去除上课周数为0的课程
		for(LessonGroup[] value : lessonGroups){
			for(LessonGroup lessonGroup : value){
				if(lessonGroup == null || lessonGroup.lessons.length < 2) continue;
				for(int k = 0; k < lessonGroup.lessons.length; k++){
					boolean flag = true;
					for(int m = 0; m < lessonGroup.lessons[k].week.length; m++){
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
		File dataFile = new File(context.getExternalFilesDir("LessonTable"),"data");
		try(ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(dataFile))){
			fos.writeObject(lessonGroups);
			fos.flush();
		}catch(Exception e){
			LogUtil.Log(e);
		}
	}
	
	
	/**
	 * 检查课程是否冲突
	 */
	public static boolean isConflict(LessonGroup[][] lessonGroups, int week, int count, Lesson lesson, int len, boolean[] booleans){
		for(int i = count; i < count + len && i < lessonGroups[0].length; i++){
			LessonGroup l = lessonGroups[week][i];
			if(l == null) continue;
			for(int j = 0;j < l.lessons.length;j++){
				// 忽略自己
				if(l.lessons[j].equals(lesson)) continue;
				for(int b = 0;b < l.lessons[j].week.length;b++){
					if(l.lessons[j].week[b] && booleans[b]){
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
