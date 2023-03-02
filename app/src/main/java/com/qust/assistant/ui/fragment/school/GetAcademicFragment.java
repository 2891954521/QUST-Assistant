package com.qust.assistant.ui.fragment.school;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetAcademicFragment extends BaseSchoolFragment{
	
	private static final String[] TYPE = {
			"","在修","未过","未修","已修",
			"校内被替代课程",
			"校内课程替代",
			"校内课程替代节点",
			"校外课程替换节点/校外认定课程",
			"校内被认定课程",
			"学业预警不审核课程"
	};
	
	/**
	 * 每组课程的开始
 	 */
	private int[] child;
	
	/**
	 * 课程是否选中
 	 */
	private boolean[] isChoose;
	
	/**
	 * 按时间排序后的课程
 	 */
	private Lesson[] lessonsSort;
	
	/**
	 * 全部课程
 	 */
	private ArrayList<Lesson> lessons;
	
	private boolean isCalculateMode;
	
	private String text;
	
	private TextView textView;
	
	private AcademicAdapter adapter;
	
	public GetAcademicFragment(){
		super();
	}
	
	public GetAcademicFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		addMenuItem(inflater, R.drawable.ic_refresh, v -> doLogin());
		
		loadData();
		
		adapter = new AcademicAdapter();
		
		isChoose = new boolean[lessonsSort.length];
		
		ExpandableListView listView = findViewById(R.id.activity_get_academic_list);
		listView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
			if(isCalculateMode){
				int len = groupPosition + 1 >= child.length ? lessonsSort.length : child[groupPosition + 1];
				for(int i = child[groupPosition]; i < len; i++){
					isChoose[i] = !isChoose[i];
				}
				calculateGpa();
				adapter.notifyDataSetInvalidated();
				return true;
			}else{
				return false;
			}
		});
		listView.setAdapter(adapter);

		textView = findViewById(R.id.activity_get_academic_text);
		textView.setText(text);
		
		addMenuItem(inflater, R.drawable.ic_calculate, v -> {
			isCalculateMode = !isCalculateMode;
			isChoose = new boolean[lessonsSort.length];
			if(isCalculateMode){
				calculateGpa();
			}else{
				adapter.notifyDataSetInvalidated();
				textView.setText(text);
			}
		});
	}
	
	@Override
	protected void doQuery(){
		sendMessage(App.UPDATE_DIALOG, "正在查询");
		
		lessons = new ArrayList<>();
		try{
			String response = WebUtil.doGet(
					loginViewModel.host + "/jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=0&layout=default",
					"JSESSIONID=" + loginViewModel.getCookie()
			);
			if(!TextUtils.isEmpty(response)){
				Matcher matcher = Pattern.compile("id=\"alertBox\"\\s?>(.*?)</div>", Pattern.DOTALL).matcher(response);
				
				if(!matcher.find()){
					throw new IllegalStateException();
				}
				
				text = Html.fromHtml(matcher.group(1)).toString().replaceAll("\t","").split(" {2,}")[1];
				
				matcher = Pattern.compile(" xfyqjd_id='(.*?)'").matcher(response);
				HashSet<String> params = new HashSet<>();
				while(matcher.find()) params.add(matcher.group(1));
				
				sendMessage(App.UPDATE_DIALOG, "正在查询课程");
				
				for(String param : params){

					response = WebUtil.doPost(
							loginViewModel.host + "/jwglxt/xsxy/xsxyqk_cxJxzxjhxfyqKcxx.html",
							"JSESSIONID=" + loginViewModel.getCookie(),
							"xfyqjd_id=" + param + "&xh_id=" + loginViewModel.name);
					
					if(TextUtils.isEmpty(response)) continue;
					
					JSONArray array = new JSONArray(response);
					
					for(int j = 0; j < array.length(); j++){
						lessons.add(new Lesson(array.getJSONObject(j)));
					}
				}
				activity.runOnUiThread(() -> {
					dialog.dismiss();
					toast("查询成功！");
					textView.setText(text);
					sortByYear();
					saveData();
					isChoose = new boolean[lessonsSort.length];
					adapter.notifyDataSetChanged();
				});
			}
		}catch(IOException | JSONException | IllegalStateException e){
			LogUtil.Log(e);
			sendMessage(App.DISMISS_TOAST, "查询失败！");
		}
	}
	
	// 载入序列化后的数据
	private void loadData(){
		try{
			child = (int[])loadData("Academic", "child");
			
			lessonsSort = (Lesson[])loadData("Academic", "lessonsSort");
			
			text = FileUtil.readFile(new File(activity.getExternalFilesDir("Academic"),"text"));
			
		}catch(Exception e){
			child = new int[TERM_NAME.length];
			lessonsSort = new Lesson[0];
			text = "";
		}
	}
	
	// 储存序列化数据
	private void saveData(){
		try{
			saveData("Academic", "child", child);
			
			saveData("Academic", "lessonsSort", lessonsSort);
			
			FileUtil.writeFile(new File(activity.getExternalFilesDir("Academic"),"text"), text);
			
		}catch(IOException e){
			LogUtil.Log(e);
		}
	}
	
	/**
	 * 按照学期排序
	 */
	private void sortByYear(){
		child = new int[TERM_NAME.length];
		lessonsSort = lessons.toArray(new Lesson[0]);
		Arrays.sort(lessonsSort, (l1, l2) -> {
			int r = Integer.compare(l1.year, l2.year);
			if(r == 0){
				return Integer.compare(l1.term, l2.term);
			}else{
				return r;
			}
		});
		int year = -1;
		int term = -1;
		int i = 0;
		for(int position = 0; position < TERM_NAME.length; position++){
			for(; i < lessonsSort.length; i++){
				Lesson lesson = lessonsSort[i];
				if(lesson.year != year || lesson.term != term){
					year = lesson.year;
					term = lesson.term;
					child[position] = i;
					break;
				}
			}
		}
	}
	
	private void calculateGpa(){
		int total = 0;
		float totalCredit = 0;
		float totalGPA = 0;
		for(int i = 0; i < isChoose.length; i++){
			if(isChoose[i]){
				total++;
				Lesson lesson = lessonsSort[i];
				float credit = Float.parseFloat(lesson.credit);
				totalCredit += credit;
				if(lesson.score != null){
					totalGPA += credit * lesson.gpa;
				}
			}
		}
		textView.setText(String.format("选择了%d门课程，总学分%.2f，平均绩点%.2f", total, totalCredit, totalGPA / totalCredit));
	}
	
	@Override
	public String getName(){
		return "学业情况查询";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_get_academic;
	}
	
	
	private class AcademicAdapter extends BaseExpandableListAdapter{
		
		@Override
		public int getGroupCount(){ return TERM_NAME.length; }
		
		@Override
		public int getChildrenCount(int groupPosition){
			if(child.length == 0){
				return 0;
			}else if(groupPosition == child.length - 1){
				return lessonsSort.length - child[groupPosition];
			}else{
				return child[groupPosition + 1] - child[groupPosition];
			}
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
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_academic_group, null);
			}
			((TextView)convertView.findViewById(R.id.item_academic_name)).setText(TERM_NAME[groupPosition]);
			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_academic_child, null);
			}
			
			int index = child[groupPosition] + childPosition;
			Lesson lesson = lessonsSort[index];
			
			CardView cardView = convertView.findViewById(R.id.item_academic_card);
			cardView.setCardBackgroundColor(isChoose[index] ? ColorUtil.BACKGROUND_COLORS[0] : activity.getResources().getColor(R.color.colorLight));
			cardView.setOnClickListener(v -> {
				if(!isCalculateMode) return;
				isChoose[index] = !isChoose[index];
				calculateGpa();
				adapter.notifyDataSetInvalidated();
			});
			
			TextView status = convertView.findViewById(R.id.item_academic_status);
			status.setTextColor(activity.getResources().getColor(lesson.status == 2 ? R.color.colorError : R.color.colorPrimaryText));
			status.setText(TYPE[lesson.status]);
			
			((TextView)convertView.findViewById(R.id.item_academic_name)).setText(lesson.name);
			
			((TextView)convertView.findViewById(R.id.item_academic_type)).setText(lesson.type);
			((TextView)convertView.findViewById(R.id.item_academic_category)).setText(lesson.category);
			((TextView)convertView.findViewById(R.id.item_academic_content)).setText(lesson.content);
			((TextView)convertView.findViewById(R.id.item_academic_credit)).setText("学分：" + lesson.credit);
			
			if(lesson.score == null){
				convertView.findViewById(R.id.item_academic_mark).setVisibility(View.GONE);
			}else{
				convertView.findViewById(R.id.item_academic_mark).setVisibility(View.VISIBLE);
				((TextView)convertView.findViewById(R.id.item_academic_score)).setText("成绩：" + lesson.score);
				((TextView)convertView.findViewById(R.id.item_academic_gpa)).setText("绩点：" + lesson.gpa);
			}

			return convertView;
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition,int childPosition){ return false; }
	}
	
	
	private static class Lesson implements Serializable{
		
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
		
		public Lesson(@NonNull JSONObject js){
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
	
	/*
    "KCH": "C206024101",         //
    "KCH_ID": "C206024101",      //
    
    "XDZT": "4",                 // 修读状态
    
    "KCMC": "工科数学分析 1",     // 课程名称
    "KCLBDM": "02",              //
    "KCYWMC": "Mathematical Analysis for Engineering 1",
    "XSXXXX": "讲课(6.0)",       //
    "KCXZMC": "必修",            // 课程。。名称
    "KCLBMC": "学科基础课",       // 课程类别名称
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
}
