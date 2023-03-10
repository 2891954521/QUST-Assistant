package com.qust.assistant.vo;

import java.io.Serializable;

public class Exam implements Serializable{
	
	private static final long serialVersionUID = 7170084777532665258L;
	
	/**
	 * 科目名称
	 */
	public String name;
	
	/**
	 * 考试地点
	 */
	public String place;
	
	/**
	 * 考试时间
	 */
	public String time;
	
}