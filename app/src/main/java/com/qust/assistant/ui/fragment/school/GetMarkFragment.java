package com.qust.assistant.ui.fragment.school;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.QustUtil.LoginUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class GetMarkFragment extends BaseSchoolFragment{
	
	private Mark[] marks;
	
	private MarkAdapter adapter;
	
	public GetMarkFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		try{
			marks = (Mark[])loadData("Mark", "mark");
		}catch(Exception e){
			marks = new Mark[0];
		}

		initYearAndTermPicker();
		
		adapter = new MarkAdapter();
		
		((ExpandableListView)findViewById(R.id.fragment_get_mark_list)).setAdapter(adapter);
	}
	
	@Override
	protected void doQuery(String session){
		
		sendMessage(App.UPDATE_DIALOG, "正在查询成绩");
		
		HashMap<String, Mark> markMap = new HashMap<>(10);
		
		String message = "查询成功";
		
		try{
			String[] y = getYearAndTerm();
			
			String response = WebUtil.doPost(
					LoginUtil.HOST + "/jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50", y[0], y[1])
			);
			
			if(!"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i<item.length(); i++){
					JSONObject j = item.getJSONObject(i);
					String name = j.getString("kcmc");
					if(!markMap.containsKey(name)){
						String cj = j.getString("cj");
						String ksxz = j.getString("ksxz");
						Mark mark = new Mark(name, ksxz, j.getString("xf"), ParamUtil.isFloat(cj) ? Float.parseFloat(cj) : 0f);
						markMap.put(name, mark);
					}
				}
			}
			
			response = WebUtil.doPost(
					LoginUtil.HOST + "/jwglxt/cjcx/cjcx_cxXsKccjList.html",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50", y[0], y[1])
			);
			
			if(!"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					JSONObject j = item.getJSONObject(i);
					String name = j.getString("kcmc");
					Mark mark;
					if(markMap.containsKey(name)){
						mark = markMap.get(name);
					}else{
						mark = new Mark(name, j.getString("xf"), 0f);
						markMap.put(name, mark);
					}
					String s = j.getString("xmblmc");
					
					if("总评".equals(s)){
						if(mark.mark == 0 && j.has("xmcj")){
							String cj = j.getString("xmcj");
							mark.mark = ParamUtil.isFloat(cj) ? Float.parseFloat(cj) : 0f;
						}
					}else{
						mark.item.add(s);
						if(j.has("xmcj")){
							mark.itemMark.add(j.getString("xmcj"));
						}else{
							mark.itemMark.add("");
						}
					}
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			message = "查询失败！";
		}
		
		marks = markMap.values().toArray(new Mark[0]);
		
		try{
			saveData("Mark","mark", marks);
		}catch(IOException ignore){ }
		
		String finalMessage = message;
		activity.runOnUiThread(() -> {
			dialog.dismiss();
			toast(finalMessage);
			adapter.notifyDataSetChanged();
		});
	}
	
	@Override
	protected String getName(){
		return "查成绩";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_get_mark;
	}
	
	
	private class MarkAdapter extends BaseExpandableListAdapter{
		
		@Override
		public int getGroupCount(){ return marks.length; }
		
		@Override
		public int getChildrenCount(int groupPosition){
			int len = marks[groupPosition].item.size();
			return len == 0 ? 0 : len + 1;
		}
		
		@Override
		public Object getGroup(int groupPosition){ return null; }
		
		@Override
		public Object getChild(int groupPosition, int childPosition){ return null; }
		
		@Override
		public long getGroupId(int groupPosition){ return 0; }
		
		@Override
		public long getChildId(int groupPosition, int childPosition){ return 0; }
		
		@Override
		public boolean hasStableIds(){ return false; }
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_mark_group, null);
			}
			Mark mark = marks[groupPosition];
			
			TextView nameText = convertView.findViewById(R.id.item_mark_name);
			nameText.setText(mark.name);
			nameText.setTextColor(activity.getResources().getColor(
					"正常考试".equals(mark.type) ? (mark.mark < 60 ? R.color.colorError : R.color.colorPrimaryText) : R.color.colorAccent
			));
			
			TextView markText = convertView.findViewById(R.id.item_mark_value);
			markText.setText(String.valueOf(mark.mark));
			markText.setTextColor(activity.getResources().getColor(
					mark.mark < 60 ? R.color.colorError : R.color.colorPrimaryText
			));
			
			((TextView)convertView.findViewById(R.id.item_mark_credit)).setText(mark.credit);
			
			((TextView)convertView.findViewById(R.id.item_mark_gpa)).setText(mark.gpa);
			
			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_mark, null);
			}
			Mark mark = marks[groupPosition];
			
			if(childPosition == 0){
				((TextView)convertView.findViewById(R.id.item_mark_item)).setText("项目");
				((TextView)convertView.findViewById(R.id.item_mark_value)).setText("成绩");
			}else{
				((TextView)convertView.findViewById(R.id.item_mark_item)).setText(mark.item.get(childPosition - 1));
				((TextView)convertView.findViewById(R.id.item_mark_value)).setText(mark.itemMark.get(childPosition - 1));
			}
			
			return convertView;
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition,int childPosition){ return false; }
		
	}
	
	
	private static class Mark implements Serializable{
		
		private static final long serialVersionUID = 1304038646483514757L;
		
		// 科目
		public String name;
		// 考试类型
		public String type;
		// 学分
		public String credit;
		// 成绩
		public float mark;
		// 绩点
		public String gpa;
		
		public ArrayList<String> item;
		
		public ArrayList<String> itemMark;
		
		public Mark(String name, String credit, float mark){
			this(name, "正常考试", credit, mark);
		}
		
		public Mark(String name, String type, String credit, float mark){
			this.name = name.trim();
			this.type = type;
			this.credit = credit;
			this.mark = mark;
			this.gpa = String.format(Locale.CHINA, "%.2f",
					mark < 60 ? 0f : ("正常考试".equals(type) ? (mark / 10 - 5) : 1f));
			item = new ArrayList<>(3);
			itemMark = new ArrayList<>(3);
		}
	}
	
}
