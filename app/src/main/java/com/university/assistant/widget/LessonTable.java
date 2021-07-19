package com.university.assistant.widget;

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

import com.university.assistant.fragment.lessontable.Lesson;
import com.university.assistant.fragment.lessontable.LessonTableData;
import com.university.assistant.util.ColorUtil;

import androidx.annotation.Nullable;

public class LessonTable extends View{
	
	private static final int LESSON_PADDING = 3;
	
	private int width,height;
	
	private int textHeight;
	
	private int week;
	
	private float downX,downY;
	
	private Paint paint,paintT;
	
	private Lesson[][] lessons;
	
	private LessonClickListener listener;
	
	public LessonTable(Context context){
		this(context,null);
	}
	
	public LessonTable(Context context,@Nullable AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public LessonTable(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		init();
	}
	
	private void init(){
		lessons = LessonTableData.getInstance().getLessons();
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
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		width = getMeasuredWidth() / 7;
		height = getMeasuredHeight() / 10;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				downY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				if(downX==event.getX()&&downY==event.getY()){
					int w = (int)(downX/width);
					int c = -1;
					Lesson.BaseLesson lesson = null;
					for(int i=0;i<lessons[w].length;){
						int len = 1;
						if(lessons[w][i]!=null){
							lesson = lessons[w][i].findLesson(week);
							if(lesson != null){
								len = lesson.len;
							}
						}
						downY -= height * len;
						if(downY < 0){
							c = i;
							break;
						}
						i += len;
					}
					if(c != -1) listener.clickLesson(w + 1, c + 1, lesson);
				}
				break;
		}
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		for(int i=0;i<lessons.length;i++){
			for(int j=0;j<lessons[0].length;j++){
				if(lessons[i][j] == null) continue;
				Lesson.BaseLesson lesson = lessons[i][j].getCurrentLesson(week);
				if(lesson == null){
					lesson = lessons[i][j].findLesson(week);
					if(lesson == null) continue;
					paint.setColor(Color.rgb(245,245,245));
					paintT.setColor(Color.rgb(204,204,204));
				}else{
					paint.setColor(ColorUtil.BACKGROUND_COLORS[lesson.color]);
					paintT.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
				}
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
					canvas.drawRoundRect(i * width,j * height,i * width + width - LESSON_PADDING,j * height + height * lesson.len - LESSON_PADDING,16,16,paint);
				}else{
					canvas.drawRoundRect(new RectF(i * width,j * height,i * width + width - LESSON_PADDING,j * height + height * lesson.len - LESSON_PADDING),16,16,paint);
				}
				// 储存每行文字
				String[] name = new String[3];
				int nameLen = splitString(lesson.name,name,width - (LESSON_PADDING << 2));
				String[] place = new String[2];
				int placeLen = splitString(lesson.place,place,width - (LESSON_PADDING << 2));
				String[] teacher = new String[2];
				int teacherLen = splitString(lesson.teacher,teacher,width - (LESSON_PADDING << 2));
				
				float x = i * width + (LESSON_PADDING << 2);
				float y = j * height + paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent;
				
				y += (height * lesson.len - textHeight*(nameLen+placeLen+teacherLen))/2;
				for(int u = 0;u<nameLen;u++){
					canvas.drawText(name[u],x,y,paintT);
					y += textHeight;
				}
				for(int u = 0;u<placeLen;u++){
					canvas.drawText(place[u],x,y,paintT);
					y += textHeight;
				}
				for(int u = 0;u<teacherLen;u++){
					canvas.drawText(teacher[u],x,y,paintT);
					y += textHeight;
				}
			}
		}
	}
	
	// 字符串分行
	private int splitString(String str,String[] split,int width){
		int len = 0;
		int begin = 0;
		for(int u = 0;u<str.length();u++){
			if(paintT.measureText(str,begin,u+1)>width){
				split[len++] = str.substring(begin,u);
				begin = u;
				if(len==split.length) break;
			}
		}
		if(len<split.length){
			if(begin==0) split[0] = str;
			else split[len] = str.substring(begin);
			len++;
		}
		return len;
	}
	
	public void setWeek(int week){
		this.week = week;
	}
	
	public void setLessons(Lesson[][] lessons){
		this.lessons = lessons;
	}
	
	public void setLessonClickListener(LessonClickListener listener){
		this.listener = listener;
	}
	
	
	public interface LessonClickListener{
		void clickLesson(int week,int count,Lesson.BaseLesson lesson);
	}
}
