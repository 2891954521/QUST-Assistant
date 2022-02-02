package com.qust.assistant.widget.slide;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class BaseSlidingLayout extends FrameLayout{
	
	/**
	 * 无效的点
	 */
	protected static final int INVALID_POINTER = -1;
	
	/**
	 * 滑动动画执行的时间 ms
	 */
	protected static final int MAX_SETTLE_DURATION = 400;
	
	/**
	 * 最小滑动距离，结合加速度来判断需要滑动的方向 dips
	 */
	protected static final int MIN_DISTANCE_FOR_FLING = 25;
	
	/**
	 * 页面边缘阴影宽度值
	 */
	protected static final int SHADOW_WIDTH = 6;
	
	/**
	 * 滑动的距离
	 */
	protected int FLING_DISTANCE;
	
	/**
	 * 最大加速度的值
	 */
	protected int MAX_VELOCITY;
	
	/**
	 * 最小加速度的值
	 */
	protected int MIN_VELOCITY;
	
	/**
	 * 开始滑动的标志距离
	 */
	protected int TOUCH_SLOP;
	
	/**
	 * 缓存的拖拽方向
	 */
	protected DragType tmpDragType;
	
	/**
	 * 默认拖动方向
	 */
	protected DragType dragType = DragType.LEFT;
	
	/**
	 * 当前活动的点Id, 有效的点的Id
	 */
	protected int activePointer = INVALID_POINTER;
	
	/**
	 * 触摸点的数据
	 */
	protected float lastX, lastY, downX, downY;
	
	protected boolean isBack, isDragging, isUnableDrag, isScrolling;
	
	/**
	 * 页面边缘阴影的宽度
	 */
	protected int shadowSize;
	
	/**
	 * 绘制阴影背景的画笔
	 */
	protected Paint shadowPaint;
	
	/**
	 * 页面边缘的阴影图
	 */
	protected Drawable shadowDrawable;
	
	/**
	 * 标记屏蔽的View
	 */
	protected View disallowView;
	
	protected Scroller scroller;
	
	protected VelocityTracker velocityTracker;
	
	protected onPageChangeListener listener;
	
	public BaseSlidingLayout(Context context){ this(context, null); }
	
	public BaseSlidingLayout(Context context, AttributeSet attributeSet){ this(context, attributeSet, 0); }
	
	public BaseSlidingLayout(Context context, AttributeSet attributeSet, int defStyleAttr){
		super(context, attributeSet, defStyleAttr);
		
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		TOUCH_SLOP = viewConfiguration.getScaledTouchSlop();
		MIN_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
		MAX_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();
		
		float density = (context.getResources().getDisplayMetrics()).density;
		
		FLING_DISTANCE = (int)(MIN_DISTANCE_FOR_FLING * density);
		
		shadowSize = (int)(SHADOW_WIDTH * density);
		
		this.scroller = new Scroller(context, t -> {
			t -= 1.0f;
			return t * t * t * t * t + 1.0f;
		});
		
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint.setColor(0xff000000);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		
		if(dragType == DragType.NONE) return false;
		
		int action = event.getAction();
		
		if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
			endDrag();
			return false;
		}
		
		if(isUnableDrag) return false;
		
		int index = event.getActionIndex();
		switch(action){
			case MotionEvent.ACTION_POINTER_UP:
				onActionUp(event, index);
				break;
				
			case MotionEvent.ACTION_MOVE:
				onActionMove(event, index);
				break;
				
			case MotionEvent.ACTION_DOWN:
				activePointer = event.getPointerId(index);
				if(activePointer == INVALID_POINTER) break;
				
				downX = lastX = event.getX(index);
				downY = lastY = event.getY(index);
				
				isUnableDrag = isInDisallow(event);
				
				if(!scroller.isFinished()) isDragging = true;
				break;
		}
		
		if(isDragging){
			event.setAction(MotionEvent.ACTION_CANCEL);
			getChildAt(0).dispatchTouchEvent(event);
		}else{
			addMovement(event);
		}
		
		return isDragging;
	}
	
	/**
	 * 判断是否需要拦截事件
	 * @param event -
	 * @param index 活动点的索引
	 */
	protected void onActionMove(MotionEvent event, int index){ }
	
	/**
	 * 对多点触控的处理
	 * @param event -
	 * @param index 抬起点的索引
	 */
	protected void onActionUp(MotionEvent event, int index){
		if(event.getPointerId(index) == activePointer){
			int newPointerIndex = index == 0 ? 1 : 0;
			lastX = event.getX(newPointerIndex);
			activePointer = event.getPointerId(newPointerIndex);
			if(velocityTracker != null) velocityTracker.clear();
		}
	}

	@Override
	public void computeScroll(){
		if(isScrolling){
			if(!scroller.isFinished() && scroller.computeScrollOffset()){
				scrollTo(scroller.getCurrX(), scroller.getCurrY());
				invalidate();
			}else{
				endScroll();
			}
		}
	}
	
	/**
	 * 结束拖拽
	 */
	protected void endDrag(){
		isDragging = false;
		isUnableDrag = false;
		activePointer = INVALID_POINTER;
		recycleVelocityTracker();
	}
	
	/**
	 * 结束滑动
	 */
	protected void endScroll(){
		if(isScrolling){
			if(listener != null) listener.onPageChange(isBack);
			scroller.abortAnimation();
			isScrolling = false;
		}
	}
	
	/**
	 * 触摸点是否在屏蔽的View内
 	 */
	protected boolean isInDisallow(MotionEvent event){
		if(disallowView==null)return false;
		int[] location = new int[2];
		disallowView.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		return !(event.getRawX() < x || event.getRawX() > (x + disallowView.getWidth()) || event.getRawY() < y || event.getRawY() > (y + disallowView.getHeight()));
	}
	
	protected void addMovement(MotionEvent event){
		if(velocityTracker == null){
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);
	}
	
	protected void recycleVelocityTracker(){
		if(velocityTracker != null){
			velocityTracker.recycle();
			velocityTracker = null;
		}
	}
	
	/**
	 * 标记屏蔽的View，按下的位置在此View内不触发滑动
	 * @param disallowView 屏蔽的View
	 */
	public void setDisallowView(View disallowView){ this.disallowView = disallowView; }
	
	/**
	 * 设置滑动方式
	 * @param dragType 滑动方式
	 */
	public void setDragType(DragType dragType){ this.dragType = dragType; }
	
	/**
	 * 禁止滑动打开菜单
	 */
	public void disableDrag(){
		tmpDragType = dragType;
		dragType = DragType.NONE;
	}
	
	/**
	 * 允许滑动打开菜单
	 */
	public void allowDrag(){
		dragType = tmpDragType;
	}
	
	/**
	 * 监听滑动返回
	 * @param listener _
	 */
	public void setOnPageChangeListener(onPageChangeListener listener){ this.listener = listener; }
	
	public interface onPageChangeListener{
		void onPageChange(boolean isBack);
	}
	
}