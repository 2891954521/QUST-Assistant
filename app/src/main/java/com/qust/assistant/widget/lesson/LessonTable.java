package com.qust.assistant.widget.lesson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.lesson.Lesson;
import com.qust.assistant.model.lesson.LessonGroup;
import com.qust.assistant.util.SettingUtil;

public class LessonTable extends ViewPager{
	
	/**
	 * 最小滑动距离
 	 */
	private int touchSlop;
	
	/**
	 * 触发长按的最小时间
	 */
	private int longPressTime;
	
	/**
	 * 是否已初始化
	 */
	private boolean hasInit;
	
	/**
	 * POP菜单出现的坐标
	 */
	private float popX, popY;
	
	/**
	 * 触摸时按下的点
	 */
	private float downX, downY;
	
	/**
	 * 上一次点击的课程位置
	 */
	private int lastWeek, lastCount;
	
	/**
	 * 点击的课程位置
	 */
	private int currentWeek, currentCount;
	
	/**
	 * 选中的课程
	 */
	private Lesson selectedLesson;
	
	
	private Runnable runnable;
	
	/**
	 * 是否清除长按回调
	 */
	private boolean clearMenu;
	
	/**
	 * POP菜单是否在显示
	 */
	private boolean isMenuShowing;
	
	
	/**
	 * 隐藏已结课程
	 */
	private boolean hideFinishLesson;
	
	private LessonMenu lessonMenu;
	
	private LessonGroup[][] lessonGroups;
	
	private LessonRender lessonRender;
	
	
	private LessonClickListener lessonClickListener;
	
	private LessonUpdateListener lessonUpdateListener;
	
	
	public LessonTable(Context context){ this(context, null); }
	
	public LessonTable(Context context, AttributeSet attrs){
		super(context, attrs);
		
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		longPressTime = ViewConfiguration.getLongPressTimeout();
		
		lessonMenu = new LessonMenu(context);
		lessonRender = new LessonRender(context);
		
		lastCount = lastWeek = -1;
		
		lessonClickListener = (week, count, lesson) -> { };
		lessonUpdateListener = () -> { };
		
		hideFinishLesson = SettingUtil.getBoolean(context.getString(R.string.KEY_HIDE_FINISH_LESSON), false);
		
		runnable = () -> {
			if(clearMenu) return;
			isMenuShowing = true;
			lastWeek = currentWeek;
			lastCount = currentCount;
			lessonMenu.show((int)popX, (int)popY);
			getAdapter().notifyDataSetChanged();
		};
	}
	
	/**
	 * 使用默认的课表数据
	 */
	public void initAdapter(){
		initAdapter(LessonTableViewModel.getLessonGroups(), LessonTableViewModel.getTotalWeek(), LessonTableViewModel.getStartDay());
	}
	
	/**
	 * 使用默认学期数据，指定课表数据
	 * @param lessonGroup 课表数据
	 */
	public void initAdapter(@Nullable LessonGroup[][] lessonGroup){
		initAdapter(lessonGroup, LessonTableViewModel.getTotalWeek(), LessonTableViewModel.getStartDay());
	}
	
	/**
	 * 指定课表数据和学期数据
	 * @param _lessonGroups 课表数据
	 * @param totalWeeks 总周数
	 * @param startDate 开学日期
	 */
	public void initAdapter(@Nullable LessonGroup[][] _lessonGroups, int totalWeeks, String startDate){
		lessonGroups = _lessonGroups == null ? LessonTableViewModel.getLessonGroups() : _lessonGroups;
		lessonRender.setTermData(startDate, totalWeeks);
		setAdapter(new LessonTableAdapter(this, totalWeeks));
		if(hasInit) lessonRender.setLessonData(lessonGroups);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		hasInit = true;
		if(lessonGroups == null){
			lessonGroups = LessonTableViewModel.getLessonGroups();
			setAdapter(new LessonTableAdapter(this, LessonTableViewModel.getTotalWeek()));
		}
		lessonRender.setMeasureData(getMeasuredWidth(), getMeasuredHeight());
		lessonRender.setLessonData(lessonGroups);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		switch(ev.getAction()){
			
			case MotionEvent.ACTION_MOVE:
				if(Math.abs(downX - ev.getX()) > touchSlop || Math.abs(downY - ev.getY()) > touchSlop){
					clearMenu();
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if(!isMenuShowing){
					clearMenu = true;
				}
				break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 处理LessonView的点击事件
	 * @return 是否重绘View
	 */
	protected boolean onLessonTouch(@NonNull MotionEvent event, int week){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				
				if(isMenuShowing) lessonMenu.dismiss();
				
				isMenuShowing = false;
				clearMenu = false;
				
				// pop菜单坐标
				popX = event.getRawX();
				popY = event.getRawY();
				
				downX = event.getX();
				downY = event.getY();
				
				int[] pos = lessonRender.getClickLesson(week, (int) downX, (int) downY);
				currentWeek = pos[0];
				currentCount = pos[1];
				
				if(currentWeek != -1 && currentCount != -1){
					if(pos[2] == 1){
						selectedLesson = lessonGroups[currentWeek][currentCount].getCurrentLesson(week + 1);
						if(selectedLesson == null){
							selectedLesson = lessonGroups[currentWeek][currentCount].findLesson(week + 1, !hideFinishLesson);
						}
					}else{
						selectedLesson = null;
					}

					postDelayed(runnable, longPressTime);
				}
				return false;
			
			case MotionEvent.ACTION_MOVE:
				if(Math.abs(downX - event.getX()) > touchSlop || Math.abs(downY - event.getY()) > touchSlop){
					clearMenu();
				}
				return false;
			
			case MotionEvent.ACTION_UP:
				if(isMenuShowing){
					return false;
				}else{
					clearMenu = true;
				}
				
				if(Math.abs(downX - event.getX()) < touchSlop && Math.abs(downY - event.getY()) < touchSlop){
					if(currentWeek != -1 && currentCount != -1){
						if(lastWeek == currentWeek && lastCount == currentCount){
							// 触发课程点击事件
							lessonClickListener.onClickLesson(currentWeek + 1, currentCount + 1, selectedLesson);
						}else{
							// 更新课程选中高亮框
							lastWeek = currentWeek;
							lastCount = currentCount;
						}
						// 通知LessonView重绘
						return true;
					}
				}
				return false;
				
			case MotionEvent.ACTION_CANCEL:
				if(isMenuShowing){
					lessonMenu.dismiss();
				}else{
					clearMenu = true;
				}
				return false;
				
			default:
				return false;
		}
	}
	
	/**
	 * 绘制LessonView
	 * @param week LessonView的周数，从0开始
	 */
	protected void drawView(Canvas canvas, int week){
		lessonRender.drawView(canvas, week);
		if(lastWeek != -1 && lastCount != -1){
			lessonRender.drawHighlightBox(canvas, lastWeek, lastCount, selectedLesson == null ? 1 : selectedLesson.len);
		}
	}
	
	/**
	 * 清除POP菜单
	 */
	public void clearMenu(){
		if(isMenuShowing){
			lessonMenu.dismiss();
		}else{
			clearMenu = true;
		}
	}
	
	
	public void setLessonClickListener(LessonClickListener listener){
		lessonClickListener = listener;
	}
	
	public void setUpdateListener(LessonUpdateListener listener){
		lessonUpdateListener = listener;
	}
	
	public interface LessonClickListener{
		void onClickLesson(int week, int count, Lesson lesson);
	}
	
	public interface LessonUpdateListener{
		void updateLesson();
	}


	private class LessonMenu extends PopupWindow{
		
		private int popWidth, popHeight;
		
		private View copy, paste, delete, addLesson;
		
		private Lesson copyLesson;
		
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
			
			popWidth = contentView.getMeasuredWidth() / 4;
			popHeight = contentView.getMeasuredHeight();
			
			copy = contentView.findViewById(R.id.menu_lesson_copy);
			paste = contentView.findViewById(R.id.menu_lesson_paste);
			delete = contentView.findViewById(R.id.menu_lesson_delete);
			addLesson = contentView.findViewById(R.id.menu_lesson_new);
			
			copy.setOnClickListener(v -> {
				copyLesson = selectedLesson.clone();
				dismiss();
			});
			
			paste.setOnClickListener(v -> {
				if(copyLesson == null) return;
				if(LessonTableViewModel.isConflict(lessonGroups, currentWeek, currentCount, copyLesson, copyLesson.len, copyLesson.week)){
					Toast.makeText(getContext(), "课程时间冲突！", Toast.LENGTH_SHORT).show();
				}else{
					if(lessonGroups[currentWeek][currentCount] == null){
						lessonGroups[currentWeek][currentCount] = new LessonGroup(currentWeek + 1, currentCount + 1);
					}
					lessonGroups[currentWeek][currentCount].addLesson(copyLesson.clone());
				}
				lessonUpdateListener.updateLesson();
				dismiss();
			});
			
			delete.setOnClickListener(v -> {
				lessonGroups[currentWeek][currentCount].removeLesson(selectedLesson);
				lessonUpdateListener.updateLesson();
				dismiss();
			});
			
			addLesson.setOnClickListener(v -> {
				if(lessonGroups[currentWeek][currentCount] == null){
					lessonGroups[currentWeek][currentCount] = new LessonGroup(currentWeek + 1, currentCount + 1);
				}
				selectedLesson = new Lesson();
				lessonGroups[currentWeek][currentCount].addLesson(selectedLesson);
				lessonClickListener.onClickLesson(currentWeek + 1, currentCount + 1, selectedLesson);
			});
			
			setContentView(contentView);
		}
		
		/**
		 * 在指定位置展示长按菜单
		 */
		public void show(int x, int y){
			
			paste.setVisibility(copyLesson == null ? View.GONE : View.VISIBLE);
			
			if(selectedLesson == null){
				copy.setVisibility(View.GONE);
				delete.setVisibility(View.GONE);
				addLesson.setVisibility(View.VISIBLE);
			}else{
				copy.setVisibility(View.VISIBLE);
				delete.setVisibility(View.VISIBLE);
				addLesson.setVisibility(selectedLesson.week[getCurrentItem()] ? View.GONE : View.VISIBLE);
			}
			showAtLocation(LessonTable.this, Gravity.START | Gravity.TOP, x - popWidth, y - popHeight);
		}
	}
	
}