package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.ParamUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class LessonTable extends ViewPager{
	
	private static final String[] WEEK_STRING = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
	
	private static final int LESSON_PADDING = 3;
	
	/*
	 * 滑动相关
	 */
	
	private int touchSlop;
	// 触摸时按下的点
	private float downX;
	
	private boolean needIntercept;
	
	private ViewPager viewPager;
	
	
	// 总周数
	private int totalWeek;
	// 开学日期
	private Calendar start;
	
	private LessonGroup[][] lessonGroups;
	
	
	// 点击的课程位置
	private int week, count;
	
	private int baseLine;
	
	private int textHeight;
	
	private int timeWidth, dateHeight;
	
	private Paint paint, paintT;
	
	// 点击的课程
	private Lesson lesson;
	
	
	/*
	 * 菜单功能
	 */
	private int longPressTime;
	
	private boolean clearMenu;
	
	private boolean isMenuShowing;
	
	private Lesson copyLesson;
	
	private LessonMenu menu;
	
	
	private LessonClickListener click;
	
	private LessonUpdateListener update;
	
	
	public LessonTable(Context context){ this(context,null); }
	
	public LessonTable(Context context,AttributeSet attrs){
		super(context,attrs);
		
		menu = new LessonMenu(context);
		
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3);
		
		paintT = new Paint();
		paintT.setDither(true);
		paintT.setAntiAlias(true);
		paintT.setSubpixelText(true);
		
		paintT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12, getResources().getDisplayMetrics()));
		
		baseLine = (int)(paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent);
		
		textHeight = (int)(paintT.getTextSize() + 3);
		
		timeWidth = ParamUtil.dp2px(context, 48);
		
		dateHeight = 40 + textHeight * 2;
		
		longPressTime = ViewConfiguration.getLongPressTimeout();
		
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		
		totalWeek = LessonData.getInstance().getTotalWeek();
		
		start = Calendar.getInstance();
		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
		if(viewPager != null)viewPager.requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				needIntercept = false;
				onTouchEvent(event);
				return false;
			case MotionEvent.ACTION_MOVE:
				if(needIntercept) return true;
				if(Math.abs(downX - event.getX()) > touchSlop){
					needIntercept = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				onTouchEvent(event);
				break;
		}
		return false;
	}
	
	// 解决滑动冲突
	public void setViewPager(ViewPager _viewPager){ viewPager = _viewPager; }
	
	public void initAdapter(LessonGroup[][] lessonGroup){
		totalWeek = LessonData.getInstance().getTotalWeek();
		if(lessonGroup == null)  lessonGroups = LessonData.getInstance().getLessonGroups();
		else lessonGroups = lessonGroup;
		setAdapter(new LessonTableAdapter());
	}
	
	public void clearMenu(){
		if(isMenuShowing){
			menu.dismiss();
		}else{
			clearMenu = true;
		}
	}
	
	private class LessonTableAdapter extends PagerAdapter{
		
		public LessonTableAdapter(){
			try{
				Date date = DateUtil.YMD.parse(LessonData.getInstance().getStartDay());
				if(date!=null)start.setTime(date);
			}catch(ParseException e){
				LogUtil.Log(e);
			}
		}
		
		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container,int position){
			ViewGroup layout = (ViewGroup)LayoutInflater.from(getContext()).inflate(R.layout.layout_timetable_week,null);
			
			LessonView lessonView = new LessonView(getContext());
			lessonView.setWeek(position + 1);
			lessonView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
			((LinearLayout)layout.findViewById(R.id.layout_timetable_week_contain)).addView(lessonView);
			container.addView(layout);
			return layout;
		}
		@Override
		public void destroyItem(@NonNull ViewGroup container,int position,@NonNull Object object){ container.removeView((View)object); }
		@Override
		public int getCount(){ return totalWeek; }
		@Override
		public boolean isViewFromObject(@NonNull View view,@NonNull Object object){ return view == object; }
		@Override
		public int getItemPosition(@NonNull Object object){
			return POSITION_NONE;
		}
	}
	
	
	public class LessonView extends View{
		
		// 当前周
		private int showWeek;
		
		private int popX, popY;
		
		// 最小的一节课的大小
		private int width, height;
		
		// 上一次点击的课程
		private int lastWeek, lastCount;
		// 按下的坐标
		private float downX, downY;
		
		private Runnable runnable;
		
		private Calendar current;
		
		public LessonView(Context context){ this(context,null); }
		
		public LessonView(Context context,@Nullable AttributeSet attrs){ this(context,attrs,0); }
		
		private LessonView(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
			super(context,attrs,defStyleAttr);
			
			current = Calendar.getInstance();
			
			lastWeek = lastCount = -1;
			
			runnable = () -> {
				if(clearMenu){
					clearMenu = false;
					return;
				}
				isMenuShowing = true;
				lastWeek = week;
				lastCount = count;
				invalidate();
				menu.show(popX,popY,showWeek);
			};
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
			width = (getMeasuredWidth() - timeWidth) / lessonGroups.length;
			height = (getMeasuredHeight() - dateHeight) / lessonGroups[0].length;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event){
			switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					isMenuShowing = false;
					
					popX = (int)event.getRawX();
					popY = (int)event.getRawY();
					
					downX = event.getX();
					downY = event.getY();
					
					int y = (int)(downY - dateHeight);
					
					week = (int)((downX - timeWidth) / width);
					count = -1;
					
					lesson = null;
					for(int i = 0;i<lessonGroups[week].length;){
						int len = 1;
						if(lessonGroups[week][i]!=null){
							lesson = lessonGroups[week][i].findLesson(showWeek);
							if(lesson!=null) len = lesson.len;
						}else lesson = null;
						y -= height * len;
						if(y<0){
							count = i;
							break;
						}
						i += len;
					}
					
					if(count != -1){
						postDelayed(runnable, longPressTime);
					}
					break;
				
				case MotionEvent.ACTION_MOVE:
					if(Math.abs(downX - event.getX()) > touchSlop || Math.abs(downY - event.getY()) > touchSlop) clearMenu();
					break;
				
				case MotionEvent.ACTION_UP:
					if(!isMenuShowing && downX == event.getX() && downY == event.getY()){
						if(count != -1){
							if(lastWeek == week && lastCount == count){
								click.clickLesson(week + 1,count + 1,lesson);
							}else{
								lastWeek = week;
								lastCount = count;
								invalidate();
							}
						}
						clearMenu();
					}
					break;
			}
			return true;
		}
		
		@Override
		protected void onDraw(Canvas canvas){
			super.onDraw(canvas);
			
			drawDate(canvas);
			
			drawTime(canvas);
			
			for(int i = 0;i<lessonGroups.length;i++){
				for(int j = 0;j<lessonGroups[0].length;j++){
					
					if(lessonGroups[i][j] == null) continue;
					
					Lesson lesson = lessonGroups[i][j].getCurrentLesson(showWeek);
					
					if(lesson == null){
						lesson = lessonGroups[i][j].findLesson(showWeek);
						if(lesson == null) continue;
						paint.setColor(Color.rgb(245,245,245));
						paintT.setColor(Color.rgb(204,204,204));
					}else{
						paint.setColor(ColorUtil.BACKGROUND_COLORS[lesson.color]);
						paintT.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
					}
					
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						canvas.drawRoundRect(i * width + timeWidth,j * height + dateHeight,i * width + width + timeWidth - LESSON_PADDING,j * height + height * lesson.len + dateHeight - LESSON_PADDING,16,16,paint);
					}else{
						canvas.drawRoundRect(new RectF(i * width + timeWidth,j * height + dateHeight,i * width + width + timeWidth - LESSON_PADDING,j * height + height * lesson.len + dateHeight - LESSON_PADDING),16,16,paint);
					}
					
					// 储存每行文字
					String[] str = new String[3 * lesson.len + 1];
					int line = splitString(lesson.name,str,width - (LESSON_PADDING << 2), 0, lesson.len + 1);
					line += splitString(lesson.place,str,width - (LESSON_PADDING << 2), lesson.len + 1, lesson.len);
					line += splitString(lesson.teacher,str,width - (LESSON_PADDING << 2),lesson.len * 2 + 1, lesson.len);
					
					int x = i * width + timeWidth + (LESSON_PADDING << 2);
					int y = j * height + dateHeight + baseLine + (height * lesson.len - textHeight * line) / 2;

					for(String s : str){
						if(s == null) continue;
						canvas.drawText(s,x,y,paintT);
						y += textHeight;
					}
				}
			}
			
			if(lastWeek != -1 && lastCount != -1){
				int len = lesson == null ? 1 : lesson.len;
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.rgb(0,176,255));
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
					canvas.drawRoundRect(lastWeek * width + timeWidth,lastCount * height + dateHeight,lastWeek * width + width + timeWidth - LESSON_PADDING,lastCount * height + height * len + dateHeight - LESSON_PADDING,16,16,paint);
				}else{
					canvas.drawRoundRect(new RectF(lastWeek * width + timeWidth,lastCount * height + dateHeight,lastWeek * width + width + timeWidth - LESSON_PADDING,lastCount * height + height * len + dateHeight - LESSON_PADDING),16,16,paint);
				}
				paint.setStyle(Paint.Style.FILL);
			}
		}
		
		private void  drawDate(Canvas canvas){
			
			Calendar c = (Calendar)start.clone();
			
			c.add(Calendar.WEEK_OF_YEAR, showWeek - 1);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
			c.add(Calendar.DATE, - dayOfWeek);
			
			int weekStart = (int)((width - paintT.measureText(WEEK_STRING[0])) / 2);
			
			int y = 20 + baseLine;
			
			for(int i=0;i<7;i++){
				String day = DateUtil.MD.format(c.getTime());
				int dayStart = (int)((width - paintT.measureText(day)) / 2);
				
				if(current.get(Calendar.DATE)==c.get(Calendar.DATE)&&current.get(Calendar.MONTH)==c.get(Calendar.MONTH)){
					paintT.setColor(ColorUtil.TEXT_COLORS[0]);
				}else{
					paintT.setColor(Color.GRAY);
				}
				
				canvas.drawText(WEEK_STRING[i], timeWidth + weekStart + i * width, y, paintT);
				canvas.drawText(day, timeWidth + dayStart + i * width, y + textHeight, paintT);

				c.add(Calendar.DATE,1);
			}
		}
		
		private void drawTime(Canvas canvas){
			paintT.setColor(Color.GRAY);
			int x = (int)((timeWidth - paintT.measureText(LessonData.Lesson_Time_Text[0][0])) / 2);
			int y = dateHeight + baseLine + (height - textHeight * 2) / 2;
			for(int i=0;i<lessonGroups[0].length;i++){
				canvas.drawText(LessonData.Lesson_Time_Text[0][i],  x, y, paintT);
				canvas.drawText(LessonData.Lesson_Time_Text[1][i],  x,y + textHeight, paintT);
				y += height;
			}
		}
		
		public void clearMenu(){
			if(isMenuShowing){
				menu.dismiss();
			}else{
				removeCallbacks(runnable);
			}
		}
		
		// 字符串分行
		private int splitString(String str,String[] split,int width, int start, int line){
			int len = 0;
			int begin = 0;
			for(int u = 0;u<str.length();u++){
				if(paintT.measureText(str,begin,u + 1) > width){
					split[start + len++] = str.substring(begin,u);
					begin = u;
					if(len==line) break;
				}
			}
			if(len < line){
				if(begin==0) split[start] = str;
				else split[start + len] = str.substring(begin);
				len++;
			}
			return len;
		}
		
		public void setWeek(int week){ showWeek = week; }
		
	}
	
	
	private class LessonMenu extends PopupWindow{
		
		private View paste;
		
		private View addLesson;
		
		private int width, height;
		
		public LessonMenu(Context context){
			super(context);
			
			setFocusable(true);
			setOutsideTouchable(true);
			setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
			setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
			setAnimationStyle(R.style.MenuAnimation);
			setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			
			View contentView = LayoutInflater.from(context).inflate(R.layout.menu_lesson, null);
			contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			
			width = contentView.getMeasuredWidth() / 4;
			height = contentView.getMeasuredHeight();
			
			View copy = contentView.findViewById(R.id.menu_lesson_copy);
			paste = contentView.findViewById(R.id.menu_lesson_paste);
			View delete = contentView.findViewById(R.id.menu_lesson_delete);
			addLesson = contentView.findViewById(R.id.menu_lesson_new);
			copy.setOnClickListener(v -> {
				copyLesson = lesson;
				dismiss();
			});
			paste.setOnClickListener(v -> {
				if(copyLesson == null) return;
				if(LessonData.getInstance().isConflict(week, count, copyLesson, copyLesson.len, copyLesson.week)){
					Toast.makeText(getContext(),"课程时间冲突！", Toast.LENGTH_SHORT).show();
				}else{
					if(lessonGroups[week][count] == null) lessonGroups[week][count] = new LessonGroup(week + 1,count + 1);
					lessonGroups[week][count].addLesson(copyLesson.clone());
				}
				update.updateLesson();
				dismiss();
			});
			delete.setOnClickListener(v -> {
				lessonGroups[week][count].removeLesson(lesson);
				update.updateLesson();
				dismiss();
			});
			addLesson.setOnClickListener(v -> {
				if(lessonGroups[week][count] == null) lessonGroups[week][count] = new LessonGroup(week + 1,count + 1);
				lesson = new Lesson();
				lessonGroups[week][count].addLesson(lesson);
				click.clickLesson(week + 1, count + 1, lesson);
			});
			setContentView(contentView);
		}
		
		public void show(int x,int y, int week){
			if(lesson != null){
				paste.setVisibility(copyLesson == null ? GONE : VISIBLE);
				addLesson.setVisibility(lesson.week[week - 1] ? GONE : VISIBLE);
				showAtLocation(LessonTable.this,Gravity.START | Gravity.TOP, x - width, y - height);
			}
		}
	}
	
	
	public void setLessonClickListener(LessonClickListener listener){
		click = listener;
	}
	
	public void setUpdateListener(LessonUpdateListener listener){
		update = listener;
	}
	
	public interface LessonClickListener{
		void clickLesson(int week,int count,Lesson lesson);
	}
	
	public interface LessonUpdateListener{
		void updateLesson();
	}
	
}