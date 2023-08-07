package com.qust.fragment.lesson;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.base.fragment.BaseFragment;
import com.qust.lesson.Lesson;
import com.qust.lesson.LessonGroup;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.view.LessonLargeViewBuilder;

import java.util.Calendar;

public class DailyLessonFragment extends BaseFragment{
	
	private LinearLayout content;
	
	private LessonTableViewModel lessonTableViewModel;
	
	public DailyLessonFragment(){
		super();
	}
	
	public DailyLessonFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		content = findViewById(R.id.dailyLesson);
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		lessonTableViewModel.getLessonTableLiveData().observe(this, update -> updateLesson());
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateLesson();
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
		
		LessonGroup[] lessonGroups = lessonTableViewModel.getLessonGroups()[lessonTableViewModel.getDayOfWeek()];
		
		Calendar c = Calendar.getInstance();
		
		int minute = (c.get(Calendar.HOUR_OF_DAY) - 8) * 60 + c.get(Calendar.MINUTE);
		
		int currentWeek = lessonTableViewModel.getCurrentWeek();
		
		String[] time = {"上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
		
		// 当前课程长度
		int pass = 0;
		
		// 当前课程
		LessonLargeViewBuilder cur = null;
		
		// 是否需要空闲占位
		boolean needEmpty = false;
		
		Lesson lesson;
		
		for(int i = 0; i < time.length; i++){
			
			minute -= lessonTableViewModel.getLessonTime()[i];
			
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
						LessonLargeViewBuilder builder = new LessonLargeViewBuilder(activity).setLesson(null).setCount(i);
						content.addView(builder.getView());
					}
				}else{
					cur = new LessonLargeViewBuilder(activity).setLesson(lesson).setCount(i);
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
	public String getName(){
		return "当日课表";
	}
}
