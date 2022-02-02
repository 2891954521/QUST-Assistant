package com.qust.assistant.widget.slide;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.qust.assistant.R;

import androidx.core.content.res.ResourcesCompat;

public class SlidingLayout extends BaseSlidingLayout{
	
	/**
	 * 是否需要绘制阴影
	 */
	private boolean hasShadow = true;
	
	/**
	 * 滑动的像素值
	 */
	private int offsetPixels;
	
	/**
	 * 阴影效果百分比
	 */
	private float ratio;
	
	/**
	 * 自定义的滑动效果回调
	 */
	private onAnimListener animListener;
	
	public SlidingLayout(Context context){ this(context, null); }
	
	public SlidingLayout(Context context, AttributeSet attributeSet){ this(context, attributeSet, 0); }
	
	public SlidingLayout(Context context, AttributeSet attributeSet, int defStyleAttr){
		super(context, attributeSet, defStyleAttr);
		setWillNotDraw(false);
		shadowDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_left, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		shadowDrawable.setBounds(0, 0, shadowSize, getMeasuredHeight());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		if(dragType == DragType.NONE) return super.onTouchEvent(event);
		
		addMovement(event);
		
		int action = event.getAction();
		int index = event.getActionIndex();
		
		switch(action){
			case MotionEvent.ACTION_DOWN:
				if(isScrolling) scroller.abortAnimation();
				isScrolling = false;
				
				activePointer = event.getPointerId(index);
				downX = lastX = event.getX(activePointer);
				break;
				
			case MotionEvent.ACTION_POINTER_DOWN:
				activePointer = event.getPointerId(index);
				lastX = event.getX(index);
				break;
				
			case MotionEvent.ACTION_MOVE:
				if(!isDragging){
					onActionMove(event, index);
					if(isUnableDrag) return false;
				}else if(activePointer != INVALID_POINTER){
					
					float x = event.getX(event.findPointerIndex(activePointer));

					float scrollX = getScrollX() + lastX - x;
					
					// 限制位置
					int left = -getWidth();
					if(scrollX < left) scrollX = left;
					else if(scrollX > 0) scrollX = 0;
					
					lastX = x + scrollX - (int)scrollX;
					
					scrollTo((int)scrollX, getScrollY());
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if(isDragging){
					velocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
					int velocity = (int) velocityTracker.getXVelocity(activePointer);
					if(activePointer != INVALID_POINTER){
						int deltaX = (int)(event.getX(event.findPointerIndex(activePointer)) - downX);
						isBack = (deltaX > FLING_DISTANCE && velocity > MIN_VELOCITY) || Math.round(-getScrollX() / (float)getWidth()) == 1;
					}
					startScroll(velocity);
					activePointer = INVALID_POINTER;
				}else{
					isBack = false;
					startScroll(0);
				}
				endDrag();
				break;
				
			case MotionEvent.ACTION_POINTER_UP:
				onActionUp(event, index);
				if(activePointer != INVALID_POINTER){
					lastX = event.getX(event.findPointerIndex(activePointer));
				}
				break;
		}
		return true;
	}
	
	@Override
	protected void onActionMove(MotionEvent event, int index){
		if(event.getPointerId(index) == INVALID_POINTER) return;
		float x = event.getX(index);
		float y = event.getY(index);
		float deltaX = x - lastX;
		if(deltaX > TOUCH_SLOP && deltaX > Math.abs(y - lastY)){
			isDragging = true;
			lastX = x;
			lastY = y;
		}else{
			isUnableDrag = true;
		}
	}
	
	private void startScroll(int velocity){
		if(getChildCount() == 0) return;
		
		int destX = isBack ? -getRight() : getLeft();
		
		isScrolling = true;
		
		int dx = destX - getScrollX();
		int dy = -getScrollY();
		
		if(dx == 0 && dy == 0){
			endScroll();
			return;
		}
		
		int width = getWidth();
		int halfWidth = width / 2;
		
		float distanceRatio = (float)Math.sin(Math.min(0.5f, Math.abs(dx) / (float)width - 0.5f) * 0.3f * Math.PI / 2.0f);
		
		int duration = velocity == 0 ? MAX_SETTLE_DURATION : Math.min(Math.round(Math.abs((halfWidth + halfWidth * distanceRatio) / velocity) * 1000) * 3, MAX_SETTLE_DURATION);
		
		scroller.startScroll(getScrollX(), getScrollY(), dx, dy, duration);
		invalidate();
	}
	
	@Override
	public void scrollTo(int x, int y){
		super.scrollTo(x, y);
		int width = getWidth();
		offsetPixels = Math.abs(x) % width;
		ratio = (float)offsetPixels / width;
		if(animListener != null) animListener.onAnimationSet(this, ratio, offsetPixels);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas){
		super.dispatchDraw(canvas);
		if(hasShadow){
			shadowPaint.setAlpha((int)((1f - ratio) * 180f));
			
			canvas.drawRect(-offsetPixels, 0f, 0f, getHeight(), shadowPaint);
			
			canvas.save();
			
			canvas.translate(-shadowSize, 0f);
			
			shadowDrawable.draw(canvas);
			
			canvas.restore();
		}
	}
	
	public void setOnAnimListener(onAnimListener listener){
		animListener = listener;
		hasShadow = false;
	}
	
	public static class AngleAnim implements onAnimListener{
		@Override
		public void onAnimationSet(View view, float ratio, int offsetPixels){
			view.setPivotX(view.getWidth() / 2f);
			view.setPivotY(view.getHeight());
			View.ROTATION.set(view, 25f * ratio);
		}
	}
	
	public interface onAnimListener{
		void onAnimationSet(View view, float ratio, int offsetPixels);
	}
}
