package com.qust.assistant.widget.swipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 可以通过滑动一定距离触发事件的TextView
 */
public class SwipeTextView extends AppCompatTextView{
	
	/**
	 * 触摸时按下的点
	 */
	private float downX;
	
	private int touchSlop;
	
	private OnSwipeListener listener;
	
	public SwipeTextView(@NonNull Context context){
		this(context, null);
	}
	
	public SwipeTextView(@NonNull Context context, @Nullable AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public SwipeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 3;
		listener = left -> { };
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				setAlpha(0.5f);
				break;
				
			case MotionEvent.ACTION_MOVE:
				float moveX = event.getX();
				if(Math.abs(moveX - downX) > touchSlop){
					listener.onSwipe(!(moveX > downX));
					downX = moveX;
				}
				break;
				
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setAlpha(1f);
				break;
		}
		return true;
	}
	
	public void setOnSwipeListener(OnSwipeListener listener){
		this.listener = listener;
	}
	
	public interface OnSwipeListener{
		/**
		 * 滑动监听
		 * @param left 是否是向左滑动
		 */
		void onSwipe(boolean left);
	}
}
