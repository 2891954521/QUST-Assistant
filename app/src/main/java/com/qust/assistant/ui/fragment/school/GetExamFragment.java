package com.qust.assistant.ui.fragment.school;

import android.content.Intent;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.QustUtil.QUSTQueryUtil;
import com.qust.assistant.vo.Exam;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		exams[selectTerm] = QUSTQueryUtil.queryExam(loginViewModel, y[0], y[1]);
		
		try{
			saveData("Exam","exam", exams);
		}catch(IOException ignored){ }
		
		sendMessage(App.NOTIFY_TOAST, "查询成功！");
	}
	
	public void pushEventToCalender(String title, String place, long startDate, long endDate){
		Intent intent = new Intent(Intent.ACTION_INSERT)
				.setData(CalendarContract.Events.CONTENT_URI)
				.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate)
				.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate)
				.putExtra(CalendarContract.Events.TITLE, title)
				.putExtra(CalendarContract.Events.EVENT_LOCATION, place)
				.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
		startActivity(intent);
	}
	
	@NonNull
	private long[] parseTime(String s){
		Pattern pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})\\((\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})");
		Matcher matcher = pattern.matcher(s);
		if(matcher.find()){
			long[] result = new long[2];
			int year = Integer.parseInt(matcher.group(1));
			int month = Integer.parseInt(matcher.group(2)) - 1;
			int day = Integer.parseInt(matcher.group(3));
			int hour = Integer.parseInt(matcher.group(4));
			int minute = Integer.parseInt(matcher.group(5));
			
			Calendar calendar = Calendar.getInstance();
			
			calendar.set(year, month, day, hour, minute);
			result[0] = calendar.getTimeInMillis();
			
			hour = Integer.parseInt(matcher.group(6));
			minute = Integer.parseInt(matcher.group(7));
			
			calendar.set(year, month, day, hour, minute);
			result[1] = calendar.getTimeInMillis();
			
			return result;
		}else{
			long time = new Date().getTime();
			return new long[]{time, time};
		}
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
			ExamViewHolder viewHolder;
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_exam, null);
				viewHolder = new ExamViewHolder(convertView);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ExamViewHolder)convertView.getTag();
			}
			viewHolder.setExam(exams[selectTerm][position]);
			return convertView;
		}
	}
	
	private class ExamViewHolder{
		
		private TextView time, name, place;
		
		private Exam exam;
		
		public ExamViewHolder(@NonNull View convertView){
			time = convertView.findViewById(R.id.item_exam_time);
			name = convertView.findViewById(R.id.item_exam_name);
			place = convertView.findViewById(R.id.item_exam_place);
			
			ImageView pushToCalender = convertView.findViewById(R.id.item_exam_add_calendar);
			pushToCalender.setOnClickListener(v -> DialogUtil.getBaseDialog(activity)
					.content("即将跳转到系统日历，不同系统功能可能不可用，是否继续")
					.onPositive(((dialog1, which) -> {
						long[] time = parseTime(exam.time);
						pushEventToCalender(exam.name, exam.place, time[0], time[1]);
					})).show());
		}
		
		public void setExam(@NonNull Exam exam){
			this.exam = exam;
			time.setText(exam.time);
			name.setText(exam.name);
			place.setText(exam.place);
		}
		
	}
	
}
