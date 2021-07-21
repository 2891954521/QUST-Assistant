package com.university.assistant.widget;

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
import android.widget.TextView;
import android.widget.Toast;

import com.university.assistant.Lesson.Lesson;
import com.university.assistant.Lesson.LessonData;
import com.university.assistant.Lesson.LessonGroup;
import com.university.assistant.R;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.DateUtil;
import com.university.assistant.util.LogUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class LessonTable extends ViewPager{
	
	private static final String[] WEEK_STRING = {"周一","周二","周三","周四","周五","周六","周日"};
	
	private static final int LESSON_PADDING = 3;
	
	private int touchSlop;
	
	// 触摸时按下的点
	private float downX;
	
	private boolean needIntercept;
	
	// 解决滑动冲突
	private ViewPager viewPager;
	
	private int totalWeek;
	
	private int textHeight;
	
	// 点击的课程位置
	private int week, count;
	
	private Paint paint, paintT;
	
	// 点击的课程
	private Lesson lesson;
	
	private LessonGroup[][] lessonGroups;
	
	// 菜单功能
	private int time;
	
	private boolean isMenuShowing;
	
	private Lesson copyLesson;
	
	private LessonMenu menu;
	
	private LessonClickListener click;
	
	private LessonUpdateListener update;
	
	
	public LessonTable(Context context){
		this(context,null);
	}
	
	public LessonTable(Context context,AttributeSet attrs){
		super(context,attrs);
		
		menu = new LessonMenu(context);
		
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		
		paintT = new Paint();
		paintT.setDither(true);
		paintT.setAntiAlias(true);
		paintT.setSubpixelText(true);
		
		paintT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,getResources().getDisplayMetrics()));
		
		textHeight = (int)(paintT.getTextSize() + 3);
		
		time = ViewConfiguration.getLongPressTimeout();
		
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		
		totalWeek = LessonData.getInstance().getTotalWeek();
		
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
	
	private class LessonTableAdapter extends PagerAdapter{
		
		private Calendar start;
		
		public LessonTableAdapter(){
			start = Calendar.getInstance();
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
			
			LinearLayout l = layout.findViewById(R.id.layout_timetable_day);
			
			Calendar current = Calendar.getInstance();
			Calendar c = (Calendar)start.clone();
			
			c.add(Calendar.WEEK_OF_YEAR,position);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
			c.add(Calendar.DATE, - dayOfWeek);
			
			for(int i=0;i<7;i++){
				TextView t = new TextView(getContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);
				lp.weight = 1;
				t.setLayoutParams(lp);
				t.setPadding(10,20,10,20);
				t.setGravity(Gravity.CENTER);
				t.setText(WEEK_STRING[i] + "\n" + DateUtil.MD.format(c.getTime()));
				t.setTextSize(12);
				if(current.get(Calendar.DATE)==c.get(Calendar.DATE)&&current.get(Calendar.MONTH)==c.get(Calendar.MONTH))
					t.setTextColor(ColorUtil.TEXT_COLORS[0]);
				else t.setTextColor(Color.GRAY);
				l.addView(t);
				c.add(Calendar.DATE,1);
			}
			
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
		
		private int width, height;
		
		private float downX, downY;
		
		private Runnable runnable;
		
		public LessonView(Context context){ this(context,null); }
		
		public LessonView(Context context,@Nullable AttributeSet attrs){ this(context,attrs,0); }
		
		private LessonView(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
			super(context,attrs,defStyleAttr);
			
			runnable = () -> {
				isMenuShowing = true;
				menu.show(popX,popY,showWeek);
			};
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
			width = getMeasuredWidth() / lessonGroups.length;
			height = getMeasuredHeight() / lessonGroups[0].length;
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
					
					int y = (int)downY;
					
					week = (int)(downX / width);
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
					
					if(count!=-1) postDelayed(runnable,time);
					break;
				
				case MotionEvent.ACTION_MOVE:
					if(downX!=event.getX() || downY!=event.getY()) removeCallbacks(runnable);
					break;
				
				case MotionEvent.ACTION_UP:
					if(!isMenuShowing && downX==event.getX() && downY==event.getY()){
						if(count!=-1) click.clickLesson(week + 1,count + 1,lesson);
					}
					removeCallbacks(runnable);
					break;
			}
			return true;
		}
		
		@Override
		protected void onDraw(Canvas canvas){
			super.onDraw(canvas);
			for(int i = 0;i<lessonGroups.length;i++){
				for(int j = 0;j<lessonGroups[0].length;j++){
					if(lessonGroups[i][j]==null) continue;
					Lesson lesson = lessonGroups[i][j].getCurrentLesson(showWeek);
					if(lesson==null){
						lesson = lessonGroups[i][j].findLesson(showWeek);
						if(lesson==null) continue;
						paint.setColor(Color.rgb(245,245,245));
						paintT.setColor(Color.rgb(204,204,204));
					}else{
						paint.setColor(ColorUtil.BACKGROUND_COLORS[lesson.color]);
						paintT.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
					}
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						canvas.drawRoundRect(i * width,j * height,i * width + width - LESSON_PADDING,j * height + height * lesson.len - LESSON_PADDING,16,16,paint);
					}else{
						canvas.drawRoundRect(new RectF(i * width,j * height,i * width + width - LESSON_PADDING,j * height + height * lesson.len - LESSON_PADDING),16,16,paint);
					}
					// 储存每行文字
					String[] str = new String[3 * lesson.len + 1];
					int line = splitString(lesson.name,str,width - (LESSON_PADDING << 2), 0, lesson.len + 1);
					line += splitString(lesson.place,str,width - (LESSON_PADDING << 2), lesson.len + 1, lesson.len);
					line += splitString(lesson.teacher,str,width - (LESSON_PADDING << 2),lesson.len * 2 + 1, lesson.len);
					
					float x = i * width + (LESSON_PADDING << 2);
					float y = j * height + paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent;
					
					y += (height * lesson.len - textHeight * line) / 2;
					for(String s : str){
						if(s == null) continue;
						canvas.drawText(s,x,y,paintT);
						y += textHeight;
					}
				}
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
		
		private View copy;
		
		private View paste;
		
		private View delete;
		
		private View addLesson;
		
		public LessonMenu(Context context){
			super(context);
			setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
			setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
			setOutsideTouchable(true);
			setFocusable(true);
			setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			View contentView = LayoutInflater.from(context).inflate(R.layout.menu_lesson,null);
			copy = contentView.findViewById(R.id.menu_lesson_copy);
			paste = contentView.findViewById(R.id.menu_lesson_paste);
			delete = contentView.findViewById(R.id.menu_lesson_delete);
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
			showAtLocation(LessonTable.this,Gravity.TOP | Gravity.START, x, y);
			copy.setVisibility(lesson == null ? GONE : VISIBLE);
			paste.setVisibility(copyLesson == null ? GONE : VISIBLE);
			delete.setVisibility(lesson == null ? GONE : VISIBLE);
			addLesson.setVisibility(lesson == null || lesson.week[week - 1] ? GONE : VISIBLE);
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