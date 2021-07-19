package com.university.assistant.widget;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.viewpager.widget.ViewPager;

public class LessonTablePager extends ViewPager{
	
	private int touchSlop;
	
	private boolean isEnd;
	
	private ViewPager viewPager;
	
	// 触摸时按下的点
	private PointF downP = new PointF();
	// 触摸时当前的点
	private PointF curP = new PointF();
	
	public LessonTablePager(Context context){
		this(context,null);
	}
	
	public LessonTablePager(Context context,AttributeSet attrs){
		super(context,attrs);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		touchSlop = configuration.getScaledTouchSlop();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
		viewPager.requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				downP.x = event.getX();
				onTouchEvent(event);
				return false;
			case MotionEvent.ACTION_MOVE:
				curP.x = event.getX();
				if(Math.abs(downP.x - curP.x)>touchSlop){
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				onTouchEvent(event);
				break;
		}
		return false;
	}
	
	public void setViewPager(ViewPager _viewPager){
		viewPager = _viewPager;
	}
	
	public void setIsEnd(boolean b){
		isEnd = b;
		//viewPager.requestDisallowInterceptTouchEvent(true);
	}
}