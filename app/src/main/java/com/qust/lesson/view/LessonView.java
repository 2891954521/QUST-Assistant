package com.qust.lesson.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class LessonView extends View{
	
	private int week;
	
	private LessonTableView lessonTableView;
	
	public LessonView(Context context){ this(context, null); }
	
	public LessonView(Context context, @Nullable AttributeSet attrs){ this(context, attrs, 0); }
	
	public LessonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}
	
	/**
	 * 设置ViewData
	 * @param week 当前展示的周
	 */
	public void setData(LessonTableView lessonTableView, int week){
		this.lessonTableView = lessonTableView;
		this.week = week;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(lessonTableView.onLessonTouch(event, week)) invalidate();
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		lessonTableView.drawView(canvas, week);
	}
}
