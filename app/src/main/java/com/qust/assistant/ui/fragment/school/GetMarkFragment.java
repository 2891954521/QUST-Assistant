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
import com.qust.assistant.util.LoginUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
						Mark mark = new Mark(name, j.getString("xf"), ParamUtil.isFloat(cj) ? Float.parseFloat(cj) : 0f);
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
						if(mark.mark == 0){
							String cj = j.getString("xmcj");
							mark.mark = ParamUtil.isFloat(cj) ? Float.parseFloat(cj) : 0f;
						}
					}else{
						mark.item.add(s);
						mark.itemMark.add(j.getString("xmcj"));
					}
				}
			}
			
			marks = markMap.values().toArray(new Mark[0]);
			
			saveData("Mark","mark", marks);
			
			activity.runOnUiThread(() -> {
				dialog.dismiss();
				toast("查询成功！");
				adapter.notifyDataSetChanged();
			});

		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			sendMessage(App.DISMISS_TOAST, "查询失败！");
		}
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
			return marks[groupPosition].item.size() + 1;
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
			((TextView)convertView.findViewById(R.id.item_mark_name)).setText(marks[groupPosition].name);
			TextView textView = convertView.findViewById(R.id.item_mark_value);
			textView.setTextColor(activity.getResources().getColor(marks[groupPosition].mark < 60 ? R.color.colorError : R.color.colorPrimaryText));
			textView.setText("成绩: " + marks[groupPosition].mark);
			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_mark, null);
			}
			Mark mark = marks[groupPosition];
			
			if(childPosition == 0){
				((TextView)convertView.findViewById(R.id.item_mark_item)).setText("学分：" + mark.credit);
				((TextView)convertView.findViewById(R.id.item_mark_value)).setText(
						mark.mark >= 60 ? String.format("绩点：%.2f",(mark.mark - 50)/10f) : "绩点：0"
				);
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
		// 科目
		public String name;
		// 学分
		public String credit;
		// 成绩
		public float mark;
		
		public ArrayList<String> item;
		
		public ArrayList<String> itemMark;

		public Mark(String name, String credit, float mark){
			this.name = name.trim();
			this.credit = credit;
			this.mark = mark;
			item = new ArrayList<>(3);
			itemMark = new ArrayList<>(3);
		}
	}
	
}
