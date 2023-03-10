package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.qust.assistant.R;

/**
 * 有三种颜色的ProgressBar
 */
public class TripleProgressBar extends View{
	
	private RectF greenRec, redRec, grayRec;
	
	private int greenColor, redColor, grayColor;
	
	private int width, height;
	
	private float green, red, max;
	
	private Paint paint;
	
	private Path path;
	
	public TripleProgressBar(Context context){
		this(context, null);
	}
	
	public TripleProgressBar(Context context, @Nullable AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public TripleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		greenColor = getResources().getColor(R.color.colorSuccess);
		redColor = getResources().getColor(R.color.colorError);
		grayColor = getResources().getColor(R.color.colorSecondaryText);
		
		path = new Path();
		greenRec = new RectF();
		redRec = new RectF();
		grayRec = new RectF();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
		
		updateRect();
		
		path.reset();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			path.addRoundRect(0, 0, width, height, height / 2f, height / 2f, Path.Direction.CW);
		}else{
			path.addRoundRect(new RectF(0, 0, width, height), height / 2f, height / 2f, Path.Direction.CW);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		canvas.clipPath(path);
		
		paint.setColor(greenColor);
		canvas.drawRect(greenRec, paint);
		
		paint.setColor(redColor);
		canvas.drawRect(redRec, paint);
		
		paint.setColor(grayColor);
		canvas.drawRect(grayRec, paint);
	}
	
	/**
	 * 设置进度
	 *
	 * @param max   最大值，为 0 时绘制为满绿色
	 * @param green 绿色的值
	 * @param red   红色的值
	 */
	public void setValues(float max, float green, float red){
		this.max = max;
		this.green = green;
		this.red = red;
		updateRect();
		invalidate();
	}
	
	private void updateRect(){
		float greenWidth = 0f;
		float redWidth = 0f;
		
		if(max != 0f){
			greenWidth = width * (green / max);
			redWidth = width * (red / max);
		}
		
		greenRec.set(0, 0, greenWidth, height);
		redRec.set(greenWidth, 0, greenWidth + redWidth, height);
		grayRec.set(greenWidth + redWidth, 0, width, height);
	}
}
