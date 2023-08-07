package com.qust.fragment.academic;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 学业情况查询课程
 */
public class LessonInfo implements Serializable{
	
	/*
		"KCH": "C206024101",         //
		"KCH_ID": "C206024101",      //
		
		"XDZT": "4",                 // 修读状态
		
		"KCMC": "工科数学分析 1",    // 课程名称
		"KCLBDM": "02",              //
		"KCYWMC": "Mathematical Analysis for Engineering 1",
		"XSXXXX": "讲课(6.0)",       //
		"KCXZMC": "必修",            // 课程。。名称
		"KCLBMC": "学科基础课",      // 课程类别名称
		"KCZT": 1,                   // 课程状态
		
		"XNM": "2020",               // 学年名
		"XNMC": "2020-2021",         // 学年名称
		"XQM": "3",                  // 学期名
		"XQMMC": "1",                // 学期名名称
		
		"CJ": "75",                  // 成绩
		"MAXCJ": "75",               // 最大成绩
		"XF": "6",                   // 学分
		"JD": 2.5,                   // 绩点
		
		"JYXDXNM": "2020",           // 建议修读学年名
		"JYXDXQM": "3"               // 建议修读学期名
		"JYXDXNMC": "2020-2021",     // 建议修读学年名称
		"JYXDXQMC": "1",             // 建议修读学期名称
		"SFJHKC": "是",              // 是否
	 */
	
	private static final long serialVersionUID = -7368871421798572097L;
	
	/**
	 * 修读状态
	 */
	public int status;
	
	/**
	 * 课程名称
	 */
	public String name;
	
	/**
	 * 课程类型
	 */
	public String type;
	
	/**
	 * 课程类别名称
	 */
	public String category;
	
	/**
	 * 课程组成
	 */
	public String content;
	
	/**
	 * 学年
	 */
	public int year;
	
	/**
	 * 学期
	 */
	public int term;
	
	/**
	 * 学分
	 */
	public String credit;
	
	/**
	 * 成绩
	 */
	public String score;
	
	/**
	 * 绩点
	 */
	public float gpa;
	
	public LessonInfo(@NonNull JSONObject js){
		try{
			name = js.getString("KCMC").trim();
			
			type = js.getString("KCXZMC");
			category = js.getString("KCLBMC");
			content = js.getString("XSXXXX");
			
			year = Integer.parseInt(js.getString(js.has("XNM") ? "XNM" : "JYXDXNM"));
			
			term = Integer.parseInt(js.getString(js.has("XQMMC") ? "XQMMC" : "JYXDXQMC"));
			
			status = Integer.parseInt(js.getString("XDZT"));
			
			credit = js.getString("XF");
			
			if(js.has("MAXCJ")) score = js.getString("MAXCJ");
			
			if(js.has("JD")) gpa = (float)js.getDouble("JD");
			
		}catch(JSONException ignore){ }
	}
}
