package com.qust.lesson;

import androidx.annotation.Nullable;

/**
 * 查询课表完成后的结果
 */
public class QueryLessonResult{
	
	/**
	 * 错误消息
	 */
	@Nullable
	public String message;
	
	/**
	 * 学期文本
	 */
	public String termText;
	
	/**
	 * 课表
	 */
	public LessonTable lessonTable;
	
	public QueryLessonResult(){
		lessonTable = new LessonTable();
	}
}