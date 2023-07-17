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
	
	public Lesson(@NonNull JSONObject json){
		this();
		try{
			
			name = json.getString("kcmc").trim();
			
			if(json.has("cdmc")) place = json.getString("cdmc").trim();
			
			if(json.has("xm")) teacher = json.getString("xm").trim();
			
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
	
	
	/*
	    {
        "ZDM": "sj",
        "SFXS": "1",
        "ZDMC": "时间"
    },
    {
        "ZDM": "cd",
        "SFXS": "1",
        "ZDMC": "场地"
    },
    {
        "ZDM": "js",
        "SFXS": "1",
        "ZDMC": "教师"
    },
    {
        "ZDM": "jszc",
        "SFXS": "0",
        "ZDMC": "教师职称"
    },
    {
        "ZDM": "jxb",
        "SFXS": "1",
        "ZDMC": "教学班"
    },
    {
        "ZDM": "xkbz",
        "SFXS": "1",
        "ZDMC": "选课备注"
    },
    {
        "ZDM": "kcxszc",
        "SFXS": "1",
        "ZDMC": "课程学时组成"
    },
    {
        "ZDM": "zhxs",
        "SFXS": "1",
        "ZDMC": "周学时"
    },
    {
        "ZDM": "zxs",
        "SFXS": "1",
        "ZDMC": "总学时"
    },
    {
        "ZDM": "khfs",
        "SFXS": "1",
        "ZDMC": "考核方式"
    },
    {
        "ZDM": "xf",
        "SFXS": "1",
        "ZDMC": "学分"
    },
    {
        "ZDM": "xq",
        "SFXS": "1",
        "ZDMC": "校区"
    },
    {
        "ZDM": "zxxx",
        "SFXS": "0",
        "ZDMC": "在线信息"
    },
    {
        "ZDM": "skfsmc",
        "SFXS": "0",
        "ZDMC": "授课方式"
    },
    {
        "ZDM": "jxbzc",
        "SFXS": "1",
        "ZDMC": "教学班组成"
    },
    {
        "ZDM": "cxbj",
        "SFXS": "0",
        "ZDMC": "重修标记"
    },
    {
        "ZDM": "zfj",
        "SFXS": "0",
        "ZDMC": "主辅讲"
    },
    {
        "ZDM": "kcxz",
        "SFXS": "0",
        "ZDMC": "课程性质"
    },
    {
        "ZDM": "kcbj",
        "SFXS": "0",
        "ZDMC": "课程标记"
    },
    {
        "ZDM": "kczxs",
        "SFXS": "0",
        "ZDMC": "课程总学时"
    }
	 */
}
