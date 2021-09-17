package com.university.assistant.lesson;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.widget.BackgroundLesson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

import androidx.annotation.Nullable;

public class LessonGroup implements Serializable, Cloneable{
	
	public static final LessonGroup EMPTY_LESSON_GROUP = new LessonGroup(0,0);
	
	// 星期几 1-7
	public int week;
	// 节次 1-10
	public int count;
	
	public Lesson[] lessons;
	
	public LessonGroup(int _week,int _count){
		week = _week;
		count = _count;
		lessons = new Lesson[0];
	}
	
	// 添加一节课程
	public void addLesson(Lesson lesson){
		lessons = Arrays.copyOf(lessons,lessons.length + 1);
		lessons[lessons.length - 1] = lesson;
	}
	
	public void removeLesson(Lesson l){
		Lesson[] tmp = new Lesson[lessons.length - 1];
		int index = 0;
		for(Lesson lesson : lessons){
			if(lesson.equals(l)) continue;
			tmp[index++] = lesson;
		}
		lessons = tmp;
	}
	
	public void removeLesson(int index){
		Lesson[] l = new Lesson[lessons.length - 1];
		int j = 0;
		for(int i=0;i<lessons.length;i++){
			if(i == index) continue;
			l[j++] = lessons[i];
		}
		lessons = l;
	}
	
	public static LessonGroup getLesson(LessonGroup lessonGroup,int week){
		if(lessonGroup == null) return EMPTY_LESSON_GROUP;
		Lesson baseLesson = lessonGroup.getCurrentLesson(week);
		return baseLesson == null ?EMPTY_LESSON_GROUP:lessonGroup;
	}
	
	// 解析js为lesson对象
	public static Lesson getLesson(JSONObject json){
		Lesson lesson = new Lesson();
		try{
			String zcd = json.getString("zcd");
			String[] sp = zcd.split(",");
			for(String a:sp){
				int type = -1;
				if(a.endsWith(")")){
					type = a.charAt(a.length()-2)=='单' ? 0 : 1;
					a = a.substring(0,a.length()-4);
				}else a = a.substring(0,a.length()-1);
				if(a.contains("-")){
					String[] p = a.split("-");
					lesson.fillLesson(true,Integer.parseInt(p[0]),Integer.parseInt(p[1]),type);
				}else{
					lesson.week[Integer.parseInt(a) - 1] = true;
				}
			}
			
			lesson.place = json.getString("cdmc").trim();
			
			lesson.name = json.getString("kcmc").trim();
			
			lesson.teacher = json.getString("xm").trim();
			
		}catch(JSONException e){
			LogUtil.Log(e);
		}
		return lesson;
	}
	
	/*
	 * 获取当前周的课程
	 */
	@Nullable
	public Lesson getCurrentLesson(int week){
		for(Lesson lesson:lessons){
			if(lesson.week.length >= week && lesson.week[week - 1])return lesson;
		}
		return null;
	}
	
	// 查找最接近当前周的课程，阿巴阿巴，说不明白
	@Nullable
	public Lesson findLesson(int week){
		if(lessons.length == 0) return null;
		for(int i=week;i>0;i--){
			for(Lesson lesson : lessons){
				if(lesson.week[i - 1]){
					return lesson;
				}
			}
		}
		for(int i=0;i<lessons[0].week.length;i++){
			for(Lesson lesson : lessons){
				if(lesson.week[i]){
					return lesson;
				}
			}
		}
		return null;
	}
	
	public static View getView(Context context, Lesson lesson, int count, int len){
		View v = LayoutInflater.from(context).inflate(R.layout.item_lesson,null);
		((TextView)v.findViewById(R.id.item_lesson_time)).setText(LessonData.Lesson_Time_Text[0][count] + "\n" + LessonData.Lesson_Time_Text[1][count + len - 1]);
		TextView n = v.findViewById(R.id.item_lesson_name);
		if(lesson == null){
			n.setText("空闲");
			n.setTextColor(Color.GRAY);
		}else{
			((BackgroundLesson)v.findViewById(R.id.item_lesson_color)).setColor(ColorUtil.TEXT_COLORS[lesson.color]);
			n.setText(lesson.name);
			n.setTextColor(Color.BLACK);
			if("".equals(lesson.place) || "".equals(lesson.teacher))
				((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place + lesson.teacher);
			else ((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place +" | " + lesson.teacher);
		}
		return v;
	}
	
//	public boolean isCurrentLesson(int hour,int minute){
//		if(hour>1) return false;
//		else if(hour==1) return minute<=50;
//		else return hour==0;
//	}
	
	@Nullable
	@Override
	public LessonGroup clone(){
		LessonGroup b = new LessonGroup(week, count);
		b.lessons = lessons.clone();
		return b;
	}
}
