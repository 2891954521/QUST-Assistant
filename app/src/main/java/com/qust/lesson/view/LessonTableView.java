package com.qust.lesson.view;

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
import androidx.viewpager.widget.ViewPager;

import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;
import com.qust.lesson.Lesson;
import com.qust.lesson.LessonGroup;
import com.qust.lesson.LessonTable;
import com.qust.lesson.LessonTableModel;

public class LessonTableView extends ViewPager{
	
	/**
	 * 最小滑动距离
 	 */
	private int touchSlop;
	
	/**
	 * 触发长按的最小时间
	 */
	private int longPressTime;
	
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
	 * 上一个点击事件是否为Move
	 * 防止异常出现的Cancel执行错误的clearMenu
	 */
	private boolean lastIsMove;
	
	/**
	 * 是否清除长按回调
	 */
	private boolean clearMenu;
	
	/**
	 * POP菜单是否在显示
	 */
	private boolean isMenuShowing;
	
	
	/**
	 * 是否已锁定
	 */
	private boolean lockLesson;
	
	/**
	 * 隐藏已结课程
	 */
	private boolean hideFinishLesson;
	
	private LessonMenu lessonMenu;
	
	private LessonTable lessonTable;
	
	private LessonRender lessonRender;
	
	private LessonClickListener lessonClickListener;
	
	private LessonUpdateListener lessonUpdateListener;
	
	
	public LessonTableView(Context context){ this(context, null); }
	
	public LessonTableView(Context context, AttributeSet attrs){
		super(context, attrs);
		
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		longPressTime = ViewConfiguration.getLongPressTimeout();
		
		lessonMenu = new LessonMenu(context);
		lessonRender = new LessonRender(context);
		
		lastCount = lastWeek = -1;
		
		lessonClickListener = (week, count, lesson) -> { };
		lessonUpdateListener = () -> { };
		
		lockLesson = SettingUtil.getBoolean(context.getString(R.string.KEY_LOCK_LESSON), false);
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
	 * 设置课表信息
	 */
	public void initAdapter(LessonTable _lessonTable){
		lessonTable = _lessonTable;
		lessonRender.setLessonTable(lessonTable);
		setAdapter(new LessonTableAdapter(this, lessonTable.getTotalWeek()));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		lessonRender.setMeasureData(getMeasuredWidth(), getMeasuredHeight());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		if(lockLesson) return super.onTouchEvent(ev);
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
				lastIsMove = false;
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
					LessonGroup lessonGroup = lessonTable.getLessons()[currentWeek][currentCount];
					if(pos[2] == 1){
						selectedLesson = lessonGroup.getCurrentLesson(week + 1);
						if(selectedLesson == null){
							selectedLesson = lessonGroup.findLesson(week + 1, lessonTable.getTotalWeek(), !hideFinishLesson);
						}
					}else{
						selectedLesson = null;
					}
					if(!lockLesson) postDelayed(runnable, longPressTime);
				}
				return false;
			
			case MotionEvent.ACTION_MOVE:
				lastIsMove = true;
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
				lastIsMove = false;
				
				if(Math.abs(downX - event.getX()) < touchSlop && Math.abs(downY - event.getY()) < touchSlop){
					if(currentWeek != -1 && currentCount != -1){
						if(lastWeek == currentWeek && lastCount == currentCount){
							// 触发课程点击事件
							if(lockLesson){
								Toast.makeText(getContext(), "课表已锁定", Toast.LENGTH_SHORT).show();
							}else{
								lessonClickListener.onClickLesson(currentWeek + 1, currentCount + 1, selectedLesson);
							}
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
				if(lastIsMove) clearMenu();
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
				if(LessonTableModel.isConflict(lessonTable, currentWeek, currentCount, copyLesson)){
					Toast.makeText(getContext(), "课程时间冲突！", Toast.LENGTH_SHORT).show();
				}else{
					LessonGroup lessonGroup = lessonTable.getLessons()[currentWeek][currentCount];
					if(lessonGroup == null){
						lessonGroup = new LessonGroup(currentWeek + 1, currentCount + 1);
					}
					lessonGroup.addLesson(copyLesson.clone());
				}
				lessonUpdateListener.updateLesson();
				dismiss();
			});
			
			delete.setOnClickListener(v -> {
				lessonTable.getLessons()[currentWeek][currentCount].removeLesson(selectedLesson);
				lessonUpdateListener.updateLesson();
				dismiss();
			});
			
			addLesson.setOnClickListener(v -> {
				LessonGroup lessonGroup = lessonTable.getLessons()[currentWeek][currentCount];
				if(lessonGroup == null){
					lessonGroup = new LessonGroup(currentWeek + 1, currentCount + 1);
				}
				selectedLesson = new Lesson();
				lessonGroup.addLesson(selectedLesson);
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
				addLesson.setVisibility((selectedLesson.week & (1L << getCurrentItem())) == 1 ? View.GONE : View.VISIBLE);
			}
			showAtLocation(LessonTableView.this, Gravity.START | Gravity.TOP, x - popWidth, y - popHeight);
		}
	}
	
}