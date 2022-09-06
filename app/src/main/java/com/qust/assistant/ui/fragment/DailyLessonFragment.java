package com.qust.assistant.ui.fragment;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.lesson.LessonView;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.ui.MainActivity;

import java.util.Calendar;

public class DailyLessonFragment extends BaseFragment{
	
	private LinearLayout content;
	
	public DailyLessonFragment(MainActivity activity){
		super(activity);
	}
	
	public DailyLessonFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		content = findViewById(R.id.dailyLesson);
		LessonTableViewModel lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		lessonTableViewModel.getUpdateLiveData().observe(this, update -> updateLesson());
	}
	
	/**
	 * 更新课程UI
	 */
	public synchronized void updateLesson(){
		if(!isCreated()) return;
		
		LayoutInflater inflater = LayoutInflater.from(activity);
		
		if(content.getChildCount() != 0){
			content.removeAllViewsInLayout();
		}
		
		LessonGroup[] lessonGroups = LessonTableViewModel.getLessonGroups()[LessonTableViewModel.getDayOfWeek()];
		
		Calendar c = Calendar.getInstance();
		
		int minute = (c.get(Calendar.HOUR_OF_DAY) - 8) * 60 + c.get(Calendar.MINUTE);
		
		int currentWeek = LessonTableViewModel.getCurrentWeek();
		
		String[] time = {"上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
		
		// 当前课程长度
		int pass = 0;
		
		// 当前课程
		LessonView cur = null;
		
		// 是否需要空闲占位
		boolean needEmpty = false;
		
		Lesson lesson;
		
		for(int i = 0; i < time.length; i++){
			
			minute -= LessonTableViewModel.getLessonTime()[i];
			
			if(pass == 0){
				// 当前位置未被跳过
				if(time[i] != null){
					// 放置表示当前时间的标题（上午，下午，晚上）
					needEmpty = true;
					TextView t = (TextView)inflater.inflate(R.layout.view_text, content, false);
					t.setText(time[i]);
					content.addView(t);
				}
				// 是否有课
				if(lessonGroups[i] == null || (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) == null){
					// 没有课的时候放置一个空闲占位
					if(needEmpty){
						needEmpty = false;
						content.addView(LessonGroup.getView(activity, null, i, 1));
					}
				}else{
					cur = new LessonView(activity, lesson, i);
					pass = lesson.len;
				}
			}
			
			if(pass > 0){
				pass--;
				if(cur == null) continue;
				if(minute > 50){
					// pass为 0 表示课程已经结束，否则为只结束了n节课
					if(pass == 0){
						cur.setTime(minute);
						content.addView(cur.getView());
					}
				}else if(minute >= 0){
					// 正在上课
					cur.setTime(minute);
					content.addView(cur.getView());
					cur = null;
				}else{
					// 课程未开始
					cur.setTime(minute);
					content.addView(cur.getView());
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
