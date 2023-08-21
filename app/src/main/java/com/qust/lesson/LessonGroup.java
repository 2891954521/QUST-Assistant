package com.qust.lesson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 */
public class LessonGroup implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 0L;
	
	/**
	 * 星期几 1-7
 	 */
	public int week;
	
	/**
	 * 节次 1-10
	 */
	public int count;
	
	public Lesson[] lessons;
	
	public LessonGroup(int _week, int _count){
		week = _week;
		count = _count;
		lessons = new Lesson[0];
	}
	
	// 添加一节课程
	public void addLesson(Lesson lesson){
		lessons = Arrays.copyOf(lessons, lessons.length + 1);
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
		for(int i = 0; i < lessons.length; i++){
			if(i == index) continue;
			l[j++] = lessons[i];
		}
		lessons = l;
	}
	
	/**
	 * 获取当前周会上的课程
	 * @param currentWeek 当前周（从 1 开始）
	 * @return 会上的课程, 没有课则为null
	 */
	@Nullable
	public Lesson getCurrentLesson(int currentWeek){
		for(Lesson lesson : lessons){
			if((lesson.week & (1L << (currentWeek - 1))) > 0) return lesson;
		}
		return null;
	}
	
	/**
	 * 查找最接近当前周会上的课程
	 *
	 * @param currentWeek 当前周（从 1 开始）
	 * @param maxWeek 最大周数
	 * @param findAll 查找全部时间
	 */
	@Nullable
	public Lesson findLesson(int currentWeek, int maxWeek, boolean findAll){
		if(lessons.length == 0) return null;
		
		// 向后查找课程
		int i = currentWeek - 1;
		long val = 1L << i;
		for(; i <= maxWeek; i++, val <<= 1){
			for(Lesson lesson : lessons){
				if((lesson.week & val) > 0) return lesson;
			}
		}

		// 向前查找课程
		if(findAll && currentWeek > 1){
			i = currentWeek - 2;
			val = 1L >> i;
			for(; i >= 0; i--, val >>= 1){
				for(Lesson lesson : lessons){
					if((lesson.week & val) > 0) return lesson;
				}
			}
		}
		return null;
	}
	
	@NonNull
	@Override
	public LessonGroup clone(){
		LessonGroup b = new LessonGroup(week, count);
		b.lessons = lessons.clone();
		return b;
	}
}
