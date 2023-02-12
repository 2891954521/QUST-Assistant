package com.qust.assistant.vo;

import androidx.annotation.Nullable;

import com.qust.assistant.model.lesson.LessonGroup;

/**
 * 查询课表完成后的结果
 */
public class QueryLessonResult{
	
	/**
	 * 消息
	 */
	@Nullable
	public String message;
	
	/**
	 * 学期文本
	 */
	public String termText;
	
	/**
	 * 开学时间
	 */
	public String startTime;
	
	/**
	 * 总周数
	 */
	public int totalWeek;
	
	/**
	 * 课表
	 */
	public LessonGroup[][] lessonGroups;
	
}