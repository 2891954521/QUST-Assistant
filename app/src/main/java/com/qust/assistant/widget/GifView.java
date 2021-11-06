package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View{
	
	private long movieStart;
	
	private Movie movie;
	
	public GifView(Context context){
		super(context);
	}
	
	public GifView(Context context,AttributeSet attrs){
		super(context,attrs);
		init(attrs);
	}
	
	public GifView(Context context,AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {

		int image = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android","src",0);
		
		movie = Movie.decodeStream(getResources().openRawResource(image));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		long nowTime = System.currentTimeMillis();
		if(movieStart == 0){
			movieStart = nowTime;
		}
		int duration = movie.duration();
		int relTime = (int) ((nowTime - movieStart) % duration);
		movie.setTime(relTime);
		movie.draw(canvas, 0, 0);
		invalidate();
	}
	
}
