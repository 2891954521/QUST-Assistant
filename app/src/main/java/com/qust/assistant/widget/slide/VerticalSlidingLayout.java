package com.qust.assistant.widget.slide;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.qust.assistant.R;

import androidx.core.content.res.ResourcesCompat;

public class VerticalSlidingLayout extends BaseSlidingLayout{
	
	private int width, height;
	
	private boolean isInit;
	
	/**
	 * 滑动的像素值
	 */
	private int offsetPixels;
	
	/**
	 * 阴影效果百分比
	 */
	private float ratio;
	
	public VerticalSlidingLayout(Context context){ this(context, null); }
	
	public VerticalSlidingLayout(Context context, AttributeSet attributeSet){ this(context, attributeSet, 0); }
	
	public VerticalSlidingLayout(Context context, AttributeSet attributeSet, int defStyleAttr){
		super(context, attributeSet, defStyleAttr);
		setWillNotDraw(false);
		shadowDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_top, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(!isInit){
			isInit = true;
			width = getMeasuredWidth();
			height = getMeasuredHeight();
			shadowDrawable.setBounds(0, 0, width, shadowSize);
			scrollTo(getScrollX(), -height);
		}
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
				downY = lastY = event.getY(activePointer);
				break;
				
			case MotionEvent.ACTION_POINTER_DOWN:
				activePointer = event.getPointerId(index);
				lastY = event.getY(index);
				break;
				
			case MotionEvent.ACTION_MOVE:
				if(!isDragging){
					onActionMove(event, index);
					if(isUnableDrag) return false;
				}else if(activePointer != INVALID_POINTER){
					
					float y = event.getY(event.findPointerIndex(activePointer));
					
					float scrollY = getScrollY() + lastY - y;
					
					if(scrollY > 0) scrollY = 0;
					else if(scrollY < -height) scrollY = -height;
					
					lastY = y;
					
					scrollTo(getScrollX(), (int)scrollY);
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if(isDragging){
					velocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
					int velocity = (int) velocityTracker.getYVelocity(activePointer);
					if(activePointer != INVALID_POINTER){
						int deltaY = (int)(event.getY(event.findPointerIndex(activePointer)) - downY);
						if(deltaY > FLING_DISTANCE){
							isBack = (velocity > MIN_VELOCITY || -getScrollY() / (float)height > 0.5f);
						}else if(-deltaY > FLING_DISTANCE){
							isBack = !(-velocity > MIN_VELOCITY || -getScrollY() / (float)height < 0.5f);
						}
					}
					startScroll(action);
					activePointer = INVALID_POINTER;
				}else{
					isBack = true;
					startScroll(0);
				}
				endDrag();
				break;
				
			case MotionEvent.ACTION_POINTER_UP:
				onActionUp(event, index);
				if(activePointer != INVALID_POINTER){
					lastY = event.getY(event.findPointerIndex(activePointer));
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
		float deltaX = Math.abs(x - lastX);
		float deltaY = Math.abs(y - lastY);
		if(deltaY > TOUCH_SLOP && deltaX < TOUCH_SLOP){
			if(deltaY > deltaX){
				isDragging = true;
				lastX = x;
				lastY = y;
			}else{
				isUnableDrag = true;
			}
		}
	}
	
	private void startScroll(int velocity){
		if(getChildCount() == 0) return;
		
		int destY = isBack ? -getBottom() : getTop();
		
		isScrolling = true;
		
		int dx = -getScrollX();
		int dy = destY - getScrollY();
		
		if(dx == 0 && dy == 0){
			endScroll();
			return;
		}
		
		int halfWidth = height / 2;
		float distanceRatio = (float)Math.sin(Math.min(1f, Math.abs(destY) / (float)height));
		int duration = velocity == 0 ? MAX_SETTLE_DURATION : Math.min(Math.round(Math.abs((halfWidth + halfWidth * distanceRatio) / velocity) * 1000) * 3, MAX_SETTLE_DURATION);

		scroller.startScroll(getScrollX(), getScrollY(), dx, dy, duration);
		invalidate();
	}
	
	@Override
	public void scrollTo(int x, int y){
		super.scrollTo(x, y);
		offsetPixels = Math.abs(y) % height;
		ratio = (float)offsetPixels / height;
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas){
		super.dispatchDraw(canvas);
		shadowPaint.setAlpha((int)((1f - ratio) * 180f));
		canvas.drawRect(0f, 0f, width, -offsetPixels, shadowPaint);
		canvas.save();
		canvas.translate(0f, -shadowSize);
		shadowDrawable.draw(canvas);
		canvas.restore();
	}
	
	public void setBack(boolean isBack){
		this.isBack = isBack;
		startScroll(150);
	}
	
}
