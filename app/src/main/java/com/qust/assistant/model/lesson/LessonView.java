package com.qust.assistant.model.lesson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.widget.LessonBackground;

/**
 * 构建课程View的类
 */
public class LessonView{
	
	private View view;
	
	private Lesson lesson;
	
	public LessonView(Context context, Lesson lesson, int count){
		this.lesson = lesson;
		
		view = LayoutInflater.from(context).inflate(R.layout.item_lesson, null);
		
		// 设置课程时间文本
		((TextView)view.findViewById(R.id.item_lesson_time)).setText(LessonTableViewModel.getLessonTimeText()[0][count] + "\n" + LessonTableViewModel.getLessonTimeText()[1][count + lesson.len - 1]);
		
		TextView n = view.findViewById(R.id.item_lesson_name);
		
		if(lesson == null){
			n.setText("空闲");
			n.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
		}else{
			n.setText(lesson.name);
			n.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
			((LessonBackground)view.findViewById(R.id.item_lesson_color)).setColor(ColorUtil.TEXT_COLORS[lesson.color]);
			if("".equals(lesson.place) || "".equals(lesson.teacher)){
				((TextView)view.findViewById(R.id.item_lesson_info)).setText(lesson.place + lesson.teacher);
			}else{
				((TextView)view.findViewById(R.id.item_lesson_info)).setText(lesson.place + " | " + lesson.teacher);
			}
		}
	}
	
	public void setTime(int minute){
		if(minute > 50){
			TextView t = view.findViewById(R.id.item_lesson_status);
			t.setAlpha(0.5f);
			t.setText("已结束");
			view.findViewById(R.id.item_lesson_time).setAlpha(0.5f);
			view.findViewById(R.id.item_lesson_name).setAlpha(0.5f);
			view.findViewById(R.id.item_lesson_info).setAlpha(0.5f);
			view.findViewById(R.id.item_lesson_color).setAlpha(0.5f);
		}else if(minute >= 0){
			TextView t = view.findViewById(R.id.item_lesson_status);
			t.setText((50 - minute) + "min后下课");
			t.setTextColor(ColorUtil.TEXT_COLORS[0]);
			TextView n = view.findViewById(R.id.item_lesson_name);
			n.getPaint().setFakeBoldText(true);
		}else{
			((TextView)view.findViewById(R.id.item_lesson_status)).setText("未开始");
		}
	}
	
	public View getView(){
		return view;
	}
}
