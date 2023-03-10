package com.qust.assistant.ui.fragment.school;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.util.QustUtil.ExamUtil;
import com.qust.assistant.vo.Exam;

import java.io.IOException;

public class GetExamFragment extends BaseSchoolFragment{
	
	/**
	 * 选中的学年
	 */
	private int selectTerm;
	
	private Exam[][] exams;
	
	public GetExamFragment(){
		super();
	}
	
	public GetExamFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		try{
			exams = (Exam[][])loadData("Exam","exam");
		}catch(Exception e){
			exams = new Exam[TERM_NAME.length][0];
		}
		
		initYearAndTermPicker();
		
		selectTerm = yearPicker.getValue();
		yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
			selectTerm = newVal;
			adapter.notifyDataSetChanged();
		});
		
		initList(new ExamAdapter());
	}
	
	@Override
	public String getName(){
		return "考试查询";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_school_query;
	}
	
	@Override
	protected void doQuery(){
		sendMessage(App.UPDATE_DIALOG, "正在查询考试");
		
		String[] y = getYearAndTerm();
		
		exams[selectTerm] = ExamUtil.queryExam(loginViewModel, y[0], y[1]);
		
		try{
			saveData("Exam","exam", exams);
		}catch(IOException ignored){ }
		
		sendMessage(App.NOTIFY_TOAST, "查询成功！");
	}
	
	private class ExamAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return exams[selectTerm].length; }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_exam, null);
			}
			Exam exam = exams[selectTerm][position];
			((TextView)convertView.findViewById(R.id.item_exam_time)).setText(exam.time);
			((TextView)convertView.findViewById(R.id.item_exam_name)).setText(exam.name);
			((TextView)convertView.findViewById(R.id.item_exam_place)).setText(exam.place);
			return convertView;
		}
	}
	
}
