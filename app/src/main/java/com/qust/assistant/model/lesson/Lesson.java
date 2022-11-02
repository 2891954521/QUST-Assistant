package com.qust.assistant.model.lesson;

import androidx.annotation.NonNull;

import com.qust.assistant.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

public class Lesson implements Serializable, Cloneable{
	
	private static final long serialVersionUID = -1649485215284483561L;
	
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
	 */
	public boolean[] week;
	
	/**
	 * 教室
 	 */
	public String place;
	
	/**
	 * 名称
 	 */
	public String name;
	
	/**
	 * 教师
 	 */
	public String teacher;
	
	public Lesson(){
		week = new boolean[24];
		place = "";
		name = "";
		teacher = "";
		len = 1;
	}
	
	public Lesson(JSONObject json){
		this();
		try{
			
			String[] sp = json.getString("zcd").split(",");
			
			for(String a : sp){
				int type = -1;
				if(a.endsWith(")")){
					type = a.charAt(a.length() - 2) == '单' ? 0 : 1;
					a = a.substring(0, a.length() - 4);
				}else a = a.substring(0, a.length() - 1);
				if(a.contains("-")){
					String[] p = a.split("-");
					fillLesson(true, Integer.parseInt(p[0]), Integer.parseInt(p[1]), type);
				}else{
					week[Integer.parseInt(a) - 1] = true;
				}
			}
			
			place = json.getString("cdmc").trim();
			
			name = json.getString("kcmc").trim();
			
			teacher = json.getString("xm").trim();
			
		}catch(JSONException e){
			LogUtil.Log(e);
		}
	}
	
	// 填充课程上课周，type -1 正常 0 单周 1 双周
	public void fillLesson(boolean b, int start, int end, int type){
		if(type == -1){
			Arrays.fill(week, start - 1, end, b);
		}else{
			for(int i = start - 1; i < end; i++){
				if(i % 2 == type) week[i] = b;
			}
		}
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
	
}
