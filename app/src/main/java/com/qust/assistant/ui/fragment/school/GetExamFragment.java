package com.qust.assistant.ui.fragment.school;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class GetExamFragment extends BaseSchoolFragment{
	
	private Exam[] exams;
	
	public GetExamFragment(MainActivity activity){
		super(activity);
	}
	
	public GetExamFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		try{
			exams = (Exam[])loadData("Exam","exam");
		}catch(Exception e){
			exams = new Exam[0];
		}
		
		initYearAndTermPicker();
		
		initList(new ExamAdapter());
		
	}
	
	@Override
	protected String getName(){
		return "考试查询";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_school_query;
	}
	
	@Override
	protected void doQuery(String session){
		Message message = new Message();
		message.obj = "正在查询考试";
		handler.sendMessage(message);
		
		try{
			String[] y = getYearAndTerm();
			
			String response = WebUtil.doPost(
					loginViewModel.HOST + "/jwglxt/kwgl/kscx_cxXsksxxIndex.html?doType=query",
					"JSESSIONID=" + session,
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50",y[0],y[1])
			);
			if(!"".equals(response)){
				ArrayList<Exam> array = new ArrayList<>();
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i=0;i<item.length();i++){
					JSONObject j = item.getJSONObject(i);
					Exam exam = new Exam();
					exam.name = j.getString("kcmc");
					exam.time = j.getString("kssj");
					exam.place = j.getString("cdmc");
					array.add(exam);
				}
				exams = array.toArray(new Exam[0]);
				
				saveData("Exam","exam", exams);
				
				sendMessage(App.NOTIFY_TOAST, "查询成功！");
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			sendMessage(App.DISMISS_TOAST, "获取课表失败！");
		}
	}
	
	private class ExamAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return exams.length; }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_exam, null);
			}
			Exam exam = exams[position];
			((TextView)convertView.findViewById(R.id.item_exam_time)).setText(exam.time);
			((TextView)convertView.findViewById(R.id.item_exam_name)).setText(exam.name);
			((TextView)convertView.findViewById(R.id.item_exam_place)).setText(exam.place);
			return convertView;
		}
	}
	
	private static class Exam implements Serializable{
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
	
}
