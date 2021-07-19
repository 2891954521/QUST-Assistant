package com.university.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.university.assistant.util.ColorUtil;

public class ColorPicker extends HorizontalScrollView{
	
	private ChooseListener listener;
	
	public ColorPicker(Context context){
		this(context,null);
	}
	
	public ColorPicker(Context context,AttributeSet attr){
		this(context,attr,0);
	}
	
	public ColorPicker(Context context,AttributeSet attr,int defAttr){
		super(context,attr,defAttr);
		addView(new Picker(context));
	}
	
	private class Picker extends View{
		
		private Paint paint;
		
		private int radius;
		
		private int dx;
		
		protected int choose;
		
		public Picker(Context context){
			super(context);
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
			radius = (getMeasuredHeight()-10)/2;
			paint.setStrokeWidth(radius/10);
			setMeasuredDimension((int)((radius*2+10)* ColorUtil.BACKGROUND_COLORS.length),getMeasuredHeight());
		}
		
		@Override
		protected void onDraw(Canvas canvas){
			paint.setStyle(Paint.Style.FILL);
			for(int j=0;j<ColorUtil.BACKGROUND_COLORS.length;j++){
				paint.setColor(ColorUtil.BACKGROUND_COLORS[j]);
				canvas.drawCircle(10+radius+j*(radius*2+10),5+radius,radius,paint);
			}
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.GRAY);
			canvas.drawCircle(10+choose*(radius*2+10) + radius,5+radius,radius,paint);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event){
			int action = event.getAction();
			if(action==MotionEvent.ACTION_DOWN){
				dx = (int)event.getX();
			}else if(action==MotionEvent.ACTION_UP){
				if(dx==(int)event.getX()){
					for(int i=0;i<ColorUtil.BACKGROUND_COLORS.length;i++){
						if(event.getX()>10+i*(radius*2+10)&&event.getX()<10+i*(radius*2+10)+radius*2){
							if(choose!=i){
								choose = i;
								if(listener!=null)listener.onChoose(i);
								invalidate();
							}
							break;
						}
					}
					return true;
				}
			}
			return true;
		}
	}
	
	public int getChoose(){ return ((Picker)getChildAt(0)).choose; }
	
	public void setChoose(int i){
		((Picker)getChildAt(0)).choose = i;
	}
	
	public void setOnChooseListener(ChooseListener listener){
		this.listener = listener;
	}
	
	public interface ChooseListener{
		void onChoose(int index);
	}
}

