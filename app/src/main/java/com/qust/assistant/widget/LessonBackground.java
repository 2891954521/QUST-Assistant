package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 当日课程颜色标签
 */
public class LessonBackground extends View{
	
	private RectF rec;
	private Paint paint;
	
	public LessonBackground(Context context){
		this(context,null);
	}
	
	public LessonBackground(Context context, @Nullable AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public LessonBackground(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context,attrs,defStyleAttr);
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		//rec = new RectF(0,0,0,0;
		//setWillNotDraw(false);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		rec = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas){
		canvas.drawRoundRect(rec, 16, 16, paint);
		super.dispatchDraw(canvas);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		//canvas.drawRoundRect(rec, 25, 0, paint);
	}
	
	public void setColor(int color){
		paint.setColor(color);
	}
}
