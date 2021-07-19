package com.university.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.university.assistant.fragment.lessontable.LessonTableData;
import com.university.assistant.util.ColorUtil;

import androidx.annotation.Nullable;

public class LessonTime extends View{
	
	private static final int TIME_PADDING = 8;
	
	private static final int ROW_COUNT = 6;
	
	private boolean[] booleans;
	
	private int textHeight;
	
	private int width,height;
	
	private Paint paint, paintT;
	
	public LessonTime(Context context){
		this(context,null);
	}
	
	public LessonTime(Context context,@Nullable AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public LessonTime(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		booleans = new boolean[LessonTableData.getInstance().getTotalWeek()];
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
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		width = getMeasuredWidth() / ROW_COUNT;
		height = width * 2 / 3;
		setMeasuredDimension(widthMeasureSpec,height + height * booleans.length / ROW_COUNT);
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		float t =  paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent;
		int weeks = LessonTableData.getInstance().getTotalWeek();
		for(int i=0;i<weeks;i++){
			if(booleans[i]){
				paint.setColor(ColorUtil.BACKGROUND_COLORS[0]);
				paintT.setColor(ColorUtil.TEXT_COLORS[0]);
			}else{
				paint.setColor(Color.rgb(245,245,245));
				paintT.setColor(Color.rgb(204,204,204));
			}
			int x = i % ROW_COUNT * width;
			int y = i / ROW_COUNT * height;
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
				canvas.drawRoundRect(x, y,x + width - TIME_PADDING,y + height - TIME_PADDING,16,16,paint);
			}else{
				canvas.drawRoundRect(new RectF(x, y,x + width - TIME_PADDING,y + height - TIME_PADDING),16,16,paint);
			}
			canvas.drawText(String.valueOf(i + 1),x + (width - paintT.measureText(String.valueOf(i + 1))) / 2,y + (height - textHeight)/2 + t,paintT);
		}
	}
	
	public boolean[] getBooleans(){
		return booleans;
	}
	
	public void setBooleans(boolean[] _booleans){
		booleans = _booleans;
	}
}
