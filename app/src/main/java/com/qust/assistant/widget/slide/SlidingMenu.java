package com.qust.assistant.widget.slide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.qust.assistant.R;

import androidx.core.content.res.ResourcesCompat;

public class SlidingMenu extends BaseSlidingLayout{
	
	private int mainX, menuX;
	
	private int mainWidth, menuWidth;
	
	private ViewGroup mainLayout, menuLayout;
	
	/**
	 * 遮罩层
	 */
	private View maskView;
	
	/**
	 * 移动动画
	 */
	private ValueAnimator animator;
	
	public SlidingMenu(Context context){
		this(context, null);
	}
	
	public SlidingMenu(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		menuX = 0;
		mainX = 0;
		shadowDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.shadow_left, (Resources.Theme)null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mainWidth = getWidth();
		shadowDrawable.setBounds(0, 0, shadowSize, getHeight());
	}
	
	@Override
	protected void onFinishInflate(){
		super.onFinishInflate();
		if(getChildCount() == 2){
			menuLayout = (ViewGroup)getChildAt(0);
			mainLayout = (ViewGroup)getChildAt(1);
			
			Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);
			menuWidth = (displayMetrics.widthPixels / 4) * 3;
			
			menuLayout.setLayoutParams(new FrameLayout.LayoutParams(menuWidth, ViewGroup.LayoutParams.MATCH_PARENT));
			
			maskView = new View(getContext());
			maskView.setBackgroundColor(Color.TRANSPARENT);
			maskView.setVisibility(View.GONE);
			maskView.setOnClickListener(v -> hideMenu());
			
			addView(maskView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			
		}else throw new RuntimeException("this view must have two child view.");
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		menuWidth = menuLayout.getWidth();
		mainWidth = mainLayout.getWidth();
		menuLayout.layout(menuX, 0, menuX + menuWidth, menuLayout.getHeight());
		mainLayout.layout(mainX, 0, mainX + mainWidth, mainLayout.getHeight());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){

		if(dragType == DragType.NONE) return super.onTouchEvent(event);

		addMovement(event);

		int index = event.getActionIndex();

		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:

				activePointer = event.getPointerId(index);

				lastX = downX = event.getX(index);
				lastY = event.getY(index);

				break;

			case MotionEvent.ACTION_POINTER_DOWN:

				activePointer = event.getPointerId(index);

				lastX = event.getX(index);

				break;

			case MotionEvent.ACTION_MOVE:
				if(isDragging){
					float x = event.getX(index);
					updateX(mainX - (int)(lastX - x));
					lastX = x;
				}else onActionMove(event, index);
				break;

			case MotionEvent.ACTION_UP:
				if(isDragging){
					velocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);

					int velocity = (int)velocityTracker.getXVelocity(activePointer);

					int delta = (int)(event.getX(event.findPointerIndex(activePointer)) - downX);

					if(mainX > 0){
						if(delta > FLING_DISTANCE){
							// 打开
							startScroll(velocity > MIN_VELOCITY || mainX > menuWidth / 2 ? menuWidth: 0, velocity);
						}else if(-delta > FLING_DISTANCE){
							// 关闭
							startScroll(-velocity > MIN_VELOCITY || mainX < menuWidth / 2 ? 0: menuWidth, velocity);
						}else{
							updateX(0);
							endScroll();
						}
					}else{
						if(delta > FLING_DISTANCE){
							// 关闭
							startScroll(velocity > MIN_VELOCITY || -mainX < menuWidth / 2 ? 0 : -menuWidth, velocity);
						}else if(-delta > FLING_DISTANCE){
							// 打开
							startScroll(-velocity > MIN_VELOCITY || -mainX > menuWidth / 2 ? -menuWidth : 0, velocity);
						}else{
							updateX(0);
							endScroll();
						}
					}
				}
				endDrag();
				break;
				
			case MotionEvent.ACTION_POINTER_UP:
				onActionUp(event, index);
				if(activePointer == INVALID_POINTER) break;
				lastX = event.getX(event.findPointerIndex(activePointer));
				break;
		}
		return true;
	}
	
	@Override
	protected void onActionMove(MotionEvent event, int index){
		float x = event.getX(index);
		float y = event.getY(index);
		float deltaX = x - lastX;
		
		if(Math.abs(deltaX) > TOUCH_SLOP && Math.abs(y - lastY) < TOUCH_SLOP){
			// 当菜单是打开的时候手势动作取反
			if(isMenuShowing() ^ ((dragType == DragType.LEFT && deltaX > 0) || (dragType == DragType.RIGHT && deltaX < 0))){
				isDragging = true;
				updateX(mainX - (int)(lastX - x));
				maskView.setVisibility(View.VISIBLE);
				lastX = x;
				lastY = y;
			}else{
				isUnableDrag = true;
			}
		}
	}

	private void startScroll(int to, int velocity){
		if(mainX == to){
			endScroll();
			return;
		}
		
		isScrolling = true;
		
		int halfWidth = menuWidth / 2;
		
		float distanceRatio = (float)(Math.min(0.5f, (float)Math.abs(to - mainX) / menuWidth - 0.5f) * 0.3f * Math.PI / 2.0f);
		
		float distance = (float)(halfWidth + halfWidth * Math.sin(distanceRatio));
		
		int duration = velocity == 0 ? MAX_SETTLE_DURATION : Math.min(3 * Math.round(1000 * Math.abs(distance / velocity)), MAX_SETTLE_DURATION);
		
		if(animator != null && animator.isRunning()){
			animator.cancel();
			animator = null;
		}
		
		maskView.setVisibility(View.VISIBLE);
		
		animator = ValueAnimator.ofInt(mainX, to);
		animator.addUpdateListener(animation -> updateX(((Number)animation.getAnimatedValue()).intValue()));
		animator.addListener(new AnimatorListenerAdapter(){
			@Override
			public void onAnimationEnd(Animator animation){ endScroll(); }
		});
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(duration);
		animator.start();
	}
	
	@Override
	protected void endScroll(){
		if(!isMenuShowing()){
			maskView.setVisibility(GONE);
		}
		isScrolling = false;
	}
	
	private void updateX(int x){
		// 限制范围
		mainX = Math.min(Math.max(x, -menuWidth), menuWidth);
		
		// 获取menu移动的距离 menu移动的距离为两个view的相差
		int menuMoveLength = mainWidth - menuWidth;
		// 获取menu当前移动的距离
		int menuCurMoveLength = (menuMoveLength * Math.abs(mainX)) / menuWidth;
		
		if(mainX < 0) menuX = 2 * menuMoveLength - menuCurMoveLength;
		else menuX = menuCurMoveLength - menuMoveLength;
		
		menuLayout.layout(menuX, 0, menuX + menuWidth, menuLayout.getHeight());
		mainLayout.layout(mainX, 0, mainX + mainWidth, mainLayout.getHeight());
		
		maskView.layout(mainX, 0, mainX + mainWidth, mainLayout.getHeight());
		
		//设置主界面阴影
		if(isMenuShowing()){
			int alpha = (int)(180f * Math.abs(mainX) / getWidth());
			maskView.setBackgroundColor(Color.argb(alpha,0,0,0));
		}else{
			maskView.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas){
		super.dispatchDraw(canvas);
		canvas.save();
		canvas.translate(-shadowSize, 0f);
		shadowDrawable.draw(canvas);
		canvas.restore();
	}
	
	public void openMenu(){
		if(!isMenuShowing()){
			maskView.setVisibility(VISIBLE);
			startScroll(this.menuWidth, 300);
		}
	}
	
	public void hideMenu(){
		if(isMenuShowing()){
			startScroll(0, 300);
		}
	}
	
	public void hideMenuNoAnim(){
		if(isMenuShowing()){
			updateX(0);
			endScroll();
		}
	}
	
	public boolean isMenuShowing(){
		return mainX != 0;
	}
}
