package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

public class BounceScrollView extends ScrollView{
	
	private View child;
	
	private float y;
	
	private Rect normal = new Rect();
	
	private boolean animationFinish = true;
	
	public BounceScrollView(Context context){
		super(context);
	}
	
	public BounceScrollView(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	
	@Override
	protected void onFinishInflate(){
		super.onFinishInflate();
		if(getChildCount()>0) child = getChildAt(0);
		else throw new RuntimeException("BounceScrollView must have one child!");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		if(!animationFinish) return super.onTouchEvent(ev);
		switch(ev.getAction()){
			case MotionEvent.ACTION_DOWN:
				y = ev.getY();
				break;
			case MotionEvent.ACTION_UP:
				y = 0;
				if(isNeedAnimation()) animation();
				break;
			case MotionEvent.ACTION_MOVE:
				final float preY = y==0?ev.getY():y;
				float nowY = ev.getY();
				int deltaY = (int)(preY - nowY);
				// 滚动
				// scrollBy(0, deltaY);
				y = nowY;
				// 当滚动到最上或者最下时就不会再滚动，这时移动布局
				if(isNeedMove()){
					if(normal.isEmpty()){
						// 保存正常的布局位置
						normal.set(child.getLeft(),child.getTop(),child.getRight(),child.getBottom());
					}
					// 移动布局
					child.layout(child.getLeft(),child.getTop() - deltaY / 2,child.getRight(),child.getBottom() - deltaY / 2);
				}break;
			default:
		}
		return super.onTouchEvent(ev);
	}
	
	// 开启动画移动
	public void animation(){
		TranslateAnimation ta = new TranslateAnimation(0,0,0,normal.top - child.getTop());
		ta.setDuration(200);
		ta.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){
				animationFinish = false;
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				child.clearAnimation();
				// 设置回到正常的布局位置
				child.layout(normal.left,normal.top,normal.right,normal.bottom);
				normal.setEmpty();
				animationFinish = true;
			}
		});
		child.startAnimation(ta);
	}
	
	// 是否需要开启动画
	public boolean isNeedAnimation(){
		return !normal.isEmpty();
	}
	
	// 是否需要移动布局
	public boolean isNeedMove(){
		int offset = child.getMeasuredHeight() - getHeight();
		int scrollY = getScrollY();
		return scrollY==0||scrollY==offset;
	}
	
}
