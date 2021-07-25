package com.university.assistant.ui.school;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.ParamUtil;
import com.university.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import androidx.annotation.Nullable;

public class GetMarkActivity extends BaseSchoolActivity{
	
	private Mark[] marks;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		marks = (Mark[])loadData("Mark","mark");
		
		if(marks == null) marks = new Mark[0];
		
		initYearAndTermPicker();
		
		initList(new MarkAdapter());
	}
	
	@Override
	protected String getName(){
		return "查成绩";
	}
	
	@Override
	protected int getLayout(){
		return R.layout.activity_school_query;
	}
	
	@Override
	protected void doQuery(String session){
		HashMap<String,Mark> markMap = new HashMap<>(10);
		try{
			String[] y = getYearAndTerm();
			String response = WebUtil.doPost(
					"http://jwglxt.qust.edu.cn/jwglxt/cjcx/cjcx_cxXsKccjList.html",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50",y[0],y[1])
			);
			
			if(response != null && !"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i=0;i<item.length();i++){
					JSONObject j = item.getJSONObject(i);
					String name = j.getString("kcmc");
					Mark mark;
					if(markMap.containsKey(name)){
						mark = markMap.get(name);
					}else{
						mark = new Mark();
						mark.name = name;
						mark.credit = j.getString("xf");
						markMap.put(name,mark);
					}
					String s = j.getString("xmblmc");
					if(s.charAt(0)=='期'){
						mark.qm = s;
						mark.qmMark = j.getString("xmcj");
					}else if(s.charAt(0)=='平'){
						mark.ps = s;
						mark.psMark = j.getString("xmcj");
					}else if(s.charAt(0)=='总'){
						mark.mark = j.getString("xmcj");
					}
				}
			}
			
			response = WebUtil.doPost(
					"http://jwglxt.qust.edu.cn/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50",y[0],y[1])
			);
			
			if(response != null && !"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0;i<item.length();i++){
					JSONObject j = item.getJSONObject(i);
					String name = j.getString("kcmc");
					if(!markMap.containsKey(name)){
						Mark mark = new Mark();
						mark.name = name;
						mark.credit = j.getString("xf");
						mark.mark = j.getString("cj");
						markMap.put(name,mark);
					}
				}
			}
			marks = markMap.values().toArray(new Mark[0]);
			
			saveData("Mark","mark", marks);
			
			runOnUiThread(() -> {
				adapter.notifyDataSetChanged();
				dialog.dismiss();
				toast("查询成功！");
			});
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			runOnUiThread(() -> {
				dialog.dismiss();
				toast("查询失败！");
			});
		}
	}
	
	private class MarkAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){
			return marks.length;
		}
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(GetMarkActivity.this).inflate(R.layout.item_mark,null);
			}
			Mark mark = marks[position];
			((TextView)convertView.findViewById(R.id.item_mark_name)).setText(mark.name);
			((TextView)convertView.findViewById(R.id.item_mark_ps)).setText(mark.ps == null ? "" : mark.ps + "：" + mark.psMark);
			((TextView)convertView.findViewById(R.id.item_mark_qm)).setText(mark.qm == null ? "" : mark.qm + "：" + mark.qmMark);
			((TextView)convertView.findViewById(R.id.item_mark_credit)).setText("学分：" + mark.credit);
			((TextView)convertView.findViewById(R.id.item_mark_GPA)).setText(ParamUtil.isFloat(mark.mark) ? String.format("绩点：%.2f",Math.max((Float.parseFloat(mark.mark) - 50)/10f,0)) : "");
			((TextView)convertView.findViewById(R.id.item_mark_mark)).setText("总成绩：" + mark.mark);
			return convertView;
		}
	}
	
	private static class Mark implements Serializable{
		
		public String name;
		
		public String credit;
		
		public String mark;
		
		public String ps;
		
		public String psMark;
		
		public String qm;
		
		public String qmMark;
	}
	
}
