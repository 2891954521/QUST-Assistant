package com.qust.lesson;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qust.assistant.R;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * 课表
 */
public class LessonTable implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 0L;
	
	/**
	 * 开学时间
	 */
	private Date startDay;
	
	/**
	 * 总周数
	 */
	private int totalWeek;
	
	private LessonGroup[][] lessons;
	
	public LessonTable(){
		lessons = new LessonGroup[7][10];
	}
	
	public LessonTable(String _startDay, int _totalWeek){
		totalWeek = _totalWeek;
		lessons = new LessonGroup[7][10];
		
		try{
			setStartDay(_startDay);
		}catch(ParseException e){
			startDay = new Date();
		}
	}
	
	public LessonTable(Date _startDay, int _totalWeek){
		startDay = _startDay;
		totalWeek = _totalWeek;
		lessons = new LessonGroup[7][10];
	}
	
	
	@NonNull
	public Date getStartDay(){
		return startDay;
	}
	
	public void setStartDay(String startDay) throws ParseException{
		setStartDay(DateUtil.YMD.parse(startDay));
	}
	
	public void setStartDay(Date startDay){
		this.startDay = startDay;
	}
	
	public int getTotalWeek(){
		return totalWeek;
	}
	
	public void setTotalWeek(int totalWeek){
		this.totalWeek = totalWeek;
	}
	
	public LessonGroup[][] getLessons(){
		return lessons;
	}
	
	public void setLessons(LessonGroup[][] lessons){
		this.lessons = lessons;
	}
	
	
	/**
	 * 从json中解析课表
	 */
	public boolean loadFromJson(@NonNull JSONObject json){
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
				
				Lesson lesson = Lesson.loadFromJson(js);
				
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
				
				if(lessons[week - 1][count - 1] == null){
					lessons[week - 1][count - 1] = new LessonGroup(week, count);
				}
				lessons[week - 1][count - 1].addLesson(lesson);
			}
			return true;
			
		}catch(JSONException e){
			LogUtil.Log(e);
			return false;
		}
	}
	
	
	@NonNull
	@Override
	public LessonTable clone(){
		try{
			// TODO: 复制此处的可变状态，这样此克隆就不能更改初始克隆的内部项
			return (LessonTable)super.clone();
		}catch(CloneNotSupportedException e){
			throw new AssertionError();
		}
	}
}
