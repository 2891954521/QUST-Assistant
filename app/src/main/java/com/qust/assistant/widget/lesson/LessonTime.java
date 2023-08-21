package com.qust.assistant.widget.lesson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.qust.lesson.LessonTableViewModel;

import java.util.Arrays;

/**
 * 设置课程上课周数的组件
 */
public class LessonTime extends View{
	
	private static final int TIME_PADDING = 8;
	
	private static final int ROW_COUNT = 6;
	
	private int textHeight;
	
	private int width, height;
	
	private float downX, downY;
	
	private boolean[] hasMove;
	
	private boolean[] booleans;
	
	private Paint paint, paintT;
	
	public LessonTime(Context context){
		this(context, null);
	}
	
	public LessonTime(Context context, @Nullable AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public LessonTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		
		booleans = new boolean[LessonTableViewModel.getInstance(context).getTotalWeek()];
		hasMove = new boolean[booleans.length];
		
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paintT = new Paint();
		paintT.setAntiAlias(true);
		paintT.setDither(true);
		paintT.setSubpixelText(true);
		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
		paintT.setTextSize(px);
		textHeight = (int)(paintT.getTextSize() + 3);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		width = getMeasuredWidth() / ROW_COUNT;
		height = width * 2 / 3;
		int col = booleans.length / ROW_COUNT + Math.min(booleans.length % ROW_COUNT , 1);
		setMeasuredDimension(widthMeasureSpec, height * col);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
		getParent().requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		int down = (int)(event.getY() / height) * ROW_COUNT + (int)(event.getX() / width) % ROW_COUNT;
		if(-1 < down && down < hasMove.length){
			switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
					if(hasMove[down]){
						booleans[down] = !booleans[down];
						hasMove[down] = false;
						invalidate();
					}
					break;
				case MotionEvent.ACTION_DOWN:
					downX = event.getX();
					downY = event.getY();
					Arrays.fill(hasMove, true);
					break;
				case MotionEvent.ACTION_UP:
					if(downX == event.getX() && downY == event.getY()){
						booleans[down] = !booleans[down];
						invalidate();
					}
					break;
			}
		}
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		float t = paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent;
		for(int i = 0; i < booleans.length; i++){
			if(booleans[i]){
				paint.setColor(Color.argb(192, 230, 244, 255));
				paintT.setColor(Color.rgb(31, 157, 208));
			}else{
				paint.setColor(Color.argb(192, 245, 245, 245));
				paintT.setColor(Color.rgb(144, 144, 144));
			}
			int x = i % ROW_COUNT * width;
			int y = i / ROW_COUNT * height;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
				canvas.drawRoundRect(x, y, x + width - TIME_PADDING, y + height - TIME_PADDING, 16, 16, paint);
			}else{
				canvas.drawRoundRect(new RectF(x, y, x + width - TIME_PADDING, y + height - TIME_PADDING), 16, 16, paint);
			}
			canvas.drawText(String.valueOf(i + 1), x + (width - paintT.measureText(String.valueOf(i + 1))) / 2, y + (height - textHeight) / 2 + t, paintT);
		}
	}
	
	public boolean[] getBooleans(){
		return booleans;
	}
	
	public long getLong(){
		long result = 0L;
		long val = 1L;
		for(int i = 0; i < booleans.length; i++, val <<= 1){
			if(booleans[i]) result |= val;
		}
		return result;
	}
	
	public void setBooleans(boolean[] _booleans){
		booleans = _booleans;
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = height * booleans.length / ROW_COUNT;
		setLayoutParams(layoutParams);
		invalidate();
	}
	
	public void setLong(long _long){
		int i = 0;
		long val = 1L;
		for(; i < booleans.length; i++, val <<= 1){
			booleans[i] = (_long & val) > 0;
		}
		
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = height * booleans.length / ROW_COUNT;
		setLayoutParams(layoutParams);
		invalidate();
	}
	
	public void setFill(){
		Arrays.fill(booleans, true);
		invalidate();
	}
	
	public void setSingle(){
		for(int i = 0; i < booleans.length; i++) booleans[i] = i % 2 == 0;
		invalidate();
	}
	
	public void setDouble(){
		for(int i = 0; i < booleans.length; i++) booleans[i] = i % 2 == 1;
		invalidate();
	}
	
}
