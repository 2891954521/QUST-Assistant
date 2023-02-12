package com.qust.assistant.widget.lesson;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class LessonView extends View{
	
	private int week;
	
	private LessonTable lessonTable;
	
	public LessonView(Context context){ this(context, null); }
	
	public LessonView(Context context, @Nullable AttributeSet attrs){ this(context, attrs, 0); }
	
	private LessonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}
	
	/**
	 * 设置ViewData
	 * @param week 当前展示的周
	 */
	public void setData(LessonTable lessonTable, int week){
		this.lessonTable = lessonTable;
		this.week = week;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(lessonTable.onLessonTouch(event, week)) invalidate();
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		lessonTable.drawView(canvas, week);
	}
}
