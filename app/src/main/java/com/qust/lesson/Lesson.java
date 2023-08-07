package com.qust.lesson;

import androidx.annotation.NonNull;

import com.qust.assistant.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 一节课程
 */
public class Lesson implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 0;
	
	/**
	 * 课程类型
	 * 0: auto 自动添加的课程
	 * 1: user 用户创建的课程
	 */
	public int type;
	
	/**
	 * 课程颜色
 	 */
	public int color;
	
	/**
	 * 课程课时
	 */
	public int len;
	
	/**
	 * 第几周有课
	 * long形式的boolean数组
	 */
	public long week;
	
	/**
	 * 课程名称
	 */
	public String name;
	
	/**
	 * 课程教室
 	 */
	public String place;
	
	/**
	 * 课程教师
 	 */
	public String teacher;
	
	public Lesson(){
		name = "";
		place = "";
		teacher = "";
		len = 1;
	}
	
	
	@NonNull
	@Override
	public Lesson clone(){
		Lesson copy;
		try{
			copy = (Lesson)super.clone();
		}catch(CloneNotSupportedException e){
			copy = new Lesson();
		}
		return copy;
	}
	
	/**
	 * 从json中解析Lesson
	 * @param json
	 * @return
	 */
	@NonNull
	public static Lesson loadFromJson(@NonNull JSONObject json){
		Lesson lesson = new Lesson();
		try{
			
			lesson.name = json.getString("kcmc").trim();
			
			if(json.has("cdmc")) lesson.place = json.getString("cdmc").trim();
			
			if(json.has("xm")) lesson.teacher = json.getString("xm").trim();
			
			String[] sp = json.getString("zcd").split(",");
			for(String a : sp){
				int type = -1;
				if(a.endsWith(")")){
					type = a.charAt(a.length() - 2) == '单' ? 0 : 1;
					a = a.substring(0, a.length() - 4);
				}else a = a.substring(0, a.length() - 1);
				
				if(a.contains("-")){
					String[] p = a.split("-");
					fillLesson(lesson, Integer.parseInt(p[0]) - 1, Integer.parseInt(p[1]), type);
				}else{
					lesson.week |= 1L << (Integer.parseInt(a) - 1);
				}
			}
			
		}catch(JSONException e){
			LogUtil.Log(json.toString(), e);
		}
		return lesson;
	}
	
	/**
	 * 填充课程上课周 (翻转数据)
	 * @param start
	 * @param end
	 * @param type -1 正常 0 单周 1 双周
	 */
	public static void fillLesson(Lesson lesson, int start, int end, int type){
		if(type == -1){
			lesson.week ^= ((1L << (end - start + 1)) - 1) << start;
		}else{
			long mask = 0L;
			for(int i = type == 0 ? start : start + 1; i < end; i += 2){
				mask |= (1L << i);
			}
			lesson.week ^= mask;
		}
	}
	
}
