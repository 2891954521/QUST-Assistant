package com.university.assistant.lesson;

import com.university.assistant.util.LogUtil;

import java.io.Serializable;
import java.util.Arrays;

import androidx.annotation.Nullable;

public class Lesson implements Serializable, Cloneable{
	// 课程颜色
	public int color;
	// 课程课时
	public int len;
	// 第几周有课
	public boolean[] week;
	// 教室
	public String place;
	// 名称
	public String name;
	// 教师
	public String teacher;
	
	public Lesson(){
		week = new boolean[24];
		place = "";
		name = "";
		teacher = "";
		len = 1;
	}
	
	// 填充课程上课周，type -1 正常 0 单周 1 双周
	public void fillLesson(boolean b, int start, int end, int type){
		if(type == -1){
			Arrays.fill(week,start - 1, end, b);
		}else{
			for(int i=start-1;i<end;i++){
				if(i%2 == type)week[i] = b;
			}
		}
	}
	
	@Nullable
	@Override
	public Lesson clone(){
		Lesson b = null;
		try{
			b = (Lesson)super.clone();
		}catch(CloneNotSupportedException e){
			LogUtil.Log(e);
		}
		return b;
	}
	
}
