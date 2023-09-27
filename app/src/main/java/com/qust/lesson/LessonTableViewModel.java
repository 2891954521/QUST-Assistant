package com.qust.lesson;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.R;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LessonTableViewModel extends AndroidViewModel{
	
	private static volatile LessonTableViewModel INSTANCE;
	
	
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
	private int[] lessonTime;
	
	/**
	 * 上下课时间文本
	 */
	private String[][] lessonTimeText;
	
	/**
	 * 当前时间表
	 */
	private int currentTime;
	
	/**
	 * 当前周 (从1开始)
	 */
	private int currentWeek;
	
	/**
	 * 当前星期 ( 0-6, 周一 —— 周日)
	 */
	private int dayOfWeek;
	
	private LessonTable lessonTable;
	
	/**
	 * 是否需要更新课表UI（课表信息变化时的回调）
	 */
	private final MutableLiveData<LessonTable> lessonTableLiveData;
	
	
	public static LessonTableViewModel getInstance(Context context){
		if(INSTANCE == null){
			synchronized(LessonTableViewModel.class){
				if(INSTANCE == null){
					INSTANCE = new LessonTableViewModel((Application)context.getApplicationContext());
				}
			}
		}
		return INSTANCE;
	}
	
	
	public LessonTableViewModel(@NonNull Application application){
		super(application);
		
		loadLesson(application);
		
		updateDate();
		
		currentTime = SettingUtil.getInt(application.getString(R.string.KEY_TIME_TABLE), 0);
		lessonTime = LESSON_TIME[currentTime];
		lessonTimeText = LESSON_TIME_TEXT[currentTime];
		lessonTableLiveData = new MutableLiveData<>(lessonTable);
	}

	
	public MutableLiveData<LessonTable> getLessonTableLiveData(){ return lessonTableLiveData; }
	
	
	/**
	 * 更新日期信息
	 */
	public void updateDate(){
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		c.setFirstDayOfWeek(Calendar.MONDAY);
		
		Calendar c2 = (Calendar)c.clone();
		
		c.setTime(lessonTable.getStartDay());
		int startWeek = c.get(Calendar.WEEK_OF_YEAR);
		
		int day = c2.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = (day == Calendar.SUNDAY ? 6 : day - 2);
		currentWeek = Math.max(1, c2.get(Calendar.WEEK_OF_YEAR) - startWeek + 1);
	}
	
	
	/**
	 * 获取当前周（从1开始）
	 */
	public int getCurrentWeek(){
		return currentWeek;
	}
	
	/**
	 * 获取当前是星期几(0-6, 周一 —— 周日)
	 */
	public int getDayOfWeek(){
		return dayOfWeek;
	}
	
	
	/**
	 * 设置开学时间
	 */
	public synchronized void setStartDay(@NonNull String _startDay) throws ParseException{
		lessonTable.setStartDay(_startDay);
		updateDate();
		lessonTableLiveData.postValue(lessonTable);
		saveLesson();
	}
	
	/**
	 * 设置开学时间
	 */
	public synchronized void setStartDay(@NonNull Date _startDay){
		lessonTable.setStartDay(_startDay);
		updateDate();
		lessonTableLiveData.postValue(lessonTable);
		saveLesson();
	}
	
	/**
	 * 设置学期总周数
	 */
	public synchronized void setTotalWeek(int _totalWeek){
		lessonTable.setTotalWeek(_totalWeek);
		lessonTableLiveData.postValue(lessonTable);
		saveLesson();
	}
	
	/**
	 * 设置课表
	 */
	public synchronized void setLessonGroups(LessonGroup[][] _lessonGroups){
		lessonTable.setLessons(_lessonGroups);
		lessonTableLiveData.postValue(lessonTable);
		saveLesson();
	}
	
	/**
	 * 设置课程时间表
	 */
	public void setCurrentTime(int index){
		currentTime = index;
		lessonTime = LESSON_TIME[index];
		lessonTimeText = LESSON_TIME_TEXT[index];
		lessonTableLiveData.postValue(lessonTable);
		SettingUtil.put(getApplication().getString(R.string.KEY_TIME_TABLE), index);
	}
	
	
	/**
	 * 获取开学时间
	 */
	public Date getStartDay(){
		return lessonTable.getStartDay();
	}
	
	/**
	 * 获取学期总周数
	 */
	public int getTotalWeek(){
		return lessonTable.getTotalWeek();
	}
	
	/**
	 * 获取课表
	 * @return
	 */
	public LessonGroup[][] getLessonGroups(){
		return lessonTable.getLessons();
	}
	
	/**
	 * 获取全部课表信息
	 * @return
	 */
	public LessonTable getLessonTable(){ return lessonTable; }
	
	/**
	 * 获取当前时间表
	 * @return
	 */
	public int getCurrentTime(){
		return currentTime;
	}
	
	/**
	 * 获取课程时间差
	 */
	public int[] getLessonTime(){
		return lessonTime;
	}
	
	/**
	 * 获取课程时间文本
	 */
	public String[][] getLessonTimeText(){
		return lessonTimeText;
	}
	
	
	/**
	 * 设置并保存课程表信息
	 */
	public void saveLessonData(LessonTable _lessonTable){
		lessonTable = _lessonTable;
		lessonTableLiveData.postValue(lessonTable);
		saveLesson();
	}
	
	
	/**
	 * 从本地文件初始化课表
	 */
	private void loadLesson(@NonNull Context context){
		File dataFile = new File(context.getFilesDir(),"lessonTables");
		
		if(dataFile.exists()){
			File[] lessonTables = dataFile.listFiles();
			if(lessonTables != null){
				for(File lessonTableFile: lessonTables){
					try{
						lessonTable = (LessonTable) FileUtils.loadData(lessonTableFile);
						break;
					}catch(IOException | ClassNotFoundException e){
						LogUtil.Log(e);
						break;
					}
				}
			}
		}else{
			dataFile.mkdirs();
		}
		
		if(lessonTable == null){
			lessonTable = new LessonTable();
		}
	}
	
	/**
	 * 序列化储存课表数据
	 */
	private void saveLesson(){
		
		// 去除上课周数为0的课程
		LessonGroup[][] lessonGroups = lessonTable.getLessons();
		for(LessonGroup[] lessonGroup : lessonGroups){
			for(int timeSlot = 0; timeSlot < lessonGroup.length; timeSlot++){
				LessonGroup group = lessonGroup[timeSlot];
				if(group == null) continue;
				if(group.lessons.length == 0) lessonGroup[timeSlot] = null;
				for(int k = 0; k < group.lessons.length; k++){
					if(group.lessons[k].week == 0){
						group.removeLesson(k);
						break;
					}
				}
			}
		}
		
		File dataFile = new File(getApplication().getFilesDir(),"lessonTables");
		if(!dataFile.exists() && !dataFile.mkdirs()) return;
		try{
			FileUtils.saveData(new File(dataFile, "lessonTable"), lessonTable);
		}catch(IOException e){
			LogUtil.Log(e);
		}
	}
}
