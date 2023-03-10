package com.qust.assistant.vo;

import androidx.annotation.NonNull;

import com.qust.assistant.util.ParamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 成绩
 */
public class Mark implements Serializable{
	
	private static final long serialVersionUID = 1304038646483514757L;
	
	/**
	 * 科目
	 */
	public String name;
	
	/**
	 * 考试类型
	 */
	public String type;
	
	/**
	 * 学分
	 */
	public String credit;
	
	/**
	 * 成绩
	 */
	public float mark;
	
	/**
	 * 绩点
 	 */
	public String gpa;
	
	/**
	 * 成绩明细名称
	 */
	public String[] items;
	
	/**
	 * 成绩明细分数
	 */
	public String[] itemMarks;
	
	private Mark(@NonNull String name, String credit, float mark, String type){
		this.name = name.trim();
		this.credit = credit;
		this.mark = mark;
		this.type = type;
		this.gpa = String.format(Locale.CHINA, "%.2f",
				mark < 60 ? 0f : ("正常考试".equals(type) ? (mark / 10 - 5) : 1f));
	}
	
	public static class Builder{
		
		private Mark mark;
		
		public ArrayList<String> items;
		
		public ArrayList<String> itemMarks;
		
		/**
		 * 解析 js 为 mark 对象
		 */
		@NonNull
		public static Builder createFromJson(@NonNull JSONObject js) throws JSONException{
			
			Mark mark = new Mark(
					js.getString("kcmc"),
					js.getString("xf"),
					js.has("cj") ? ParamUtil.parseFloat(js.getString("cj")) : 0f,
					js.has("ksxz") ? js.getString("ksxz") : "正常考试"
			);
			
			return new Builder(mark);
		}
		
		private Builder(Mark _mark){
			mark = _mark;
			items = new ArrayList<>(4);
			itemMarks = new ArrayList<>(4);
		}
		
		/**
		 * 添加一条成绩明细
		 */
		public void addItemMark(@NonNull JSONObject js) throws JSONException{
			if(!js.has("xmblmc")) return;
			
			String itemName = js.getString("xmblmc");
			
			if("总评".equals(itemName)){
				if(mark.mark == 0f && js.has("xmcj")){
					mark.mark = ParamUtil.parseFloat(js.getString("xmcj"));
				}
			}else{
				items.add(itemName);
				itemMarks.add(js.has("xmcj") ? js.getString("xmcj") : "");
			}
		}
		
		public Mark build(){
			mark.items = items.toArray(new String[0]);
			mark.itemMarks = itemMarks.toArray(new String[0]);
			return mark;
		}
	}
	
}
