package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PointView extends View{
	
	private Paint paint;
	
	private int width,height;
	
	private int radius;
	
	private int padding;
	
	public PointView(Context context){
		super(context);
		init();
	}
	
	public PointView(Context context,AttributeSet attrs){
		super(context,attrs);
		init();
	}
	
	public PointView(Context context,AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		init();
	}
	
	private void init(){
		paint = new Paint();
		paint.setStrokeWidth(3);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		radius = 24;
		padding = 8;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		paint.setColor(Color.parseColor("#3399fe"));
		canvas.drawCircle(width/2f,radius + padding,radius,paint);
		paint.setColor(Color.parseColor("#dadbdd"));
		canvas.drawLine(width/2f,(radius + padding) * 2, width/2f,height - (radius + padding) * 2,paint);
		paint.setColor(Color.parseColor("#14bc7f"));
		canvas.drawCircle(width/2f,height - radius - padding,radius,paint);
	}
	
}
