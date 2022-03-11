package com.qust.assistant.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.ColorUtil;

import java.util.Calendar;

public class DailyLessonFragment extends BaseFragment{
	
	public DailyLessonFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		updateLesson(inflater);
	}
	
	public void updateLesson(LayoutInflater inflater){
		layout.removeAllViews();
		
		LessonGroup[] lessonGroups = LessonData.getInstance().getLessonGroups()[LessonData.getInstance().getWeek()];
		
		Calendar c = Calendar.getInstance();
		
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		int currentWeek = LessonData.getInstance().getCurrentWeek();
		
		String[] time = {"上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
		
		int pass = 0;
		
		boolean needEmpty = false;
		
		View cur = null;
		
		Lesson lesson;
		
		for(int i = 0; i < time.length; i++){
			if(pass == 0){
				if(time[i] != null){
					needEmpty = true;
					TextView t = (TextView)inflater.inflate(R.layout.view_text, null);
					t.setText(time[i]);
					layout.addView(t);
				}
				if(lessonGroups[i] == null || (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) == null){
					if(needEmpty){
						needEmpty = false;
						layout.addView(LessonGroup.getView(activity, null, i, 1));
					}
				}else{
					cur = LessonGroup.getView(activity, lesson, i, lesson.len);
					pass = lesson.len;
				}
			}
			
			h -= LessonData.Lesson_Time[i][0];
			m -= LessonData.Lesson_Time[i][1];
			
			if(m < 0){ h--; m += 60; }
			
			if(pass > 0){
				pass--;
				if(cur == null) continue;
				if(h > 0){
					if(pass == 0){
						((TextView)cur.findViewById(R.id.item_lesson_status)).setText("已结束");
						layout.addView(cur);
					}
				}else if(h == 0){
					if(m > 50){
						if(pass == 0){
							((TextView)cur.findViewById(R.id.item_lesson_status)).setText("已结束");
							layout.addView(cur);
						}
					}else{
						TextView t = cur.findViewById(R.id.item_lesson_status);
						t.setText((50 - m) + "min后下课");
						t.setTextColor(ColorUtil.TEXT_COLORS[0]);
						TextView n = cur.findViewById(R.id.item_lesson_name);
						n.getPaint().setFakeBoldText(true);
						layout.addView(cur);
						cur = null;
					}
				}else{
					((TextView)cur.findViewById(R.id.item_lesson_status)).setText("未开始");
					layout.addView(cur);
					cur = null;
				}
			}
		}
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_daily_lesson;
	}
	
	@Override
	protected String getName(){
		return "当日课表";
	}
}
