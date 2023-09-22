package com.qust.lesson.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.R;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.lesson.Lesson;
import com.qust.lesson.LessonTable;
import com.qust.lesson.LessonTableViewModel;

import java.util.Calendar;

/**
 * 总课表界面的渲染器
 */
public class LessonRender{
	
	private static final String[] WEEK_STRING = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
	
	/**
	 * 课程间距
	 */
	private static final int LESSON_PADDING = 3;
	
	/**
	 * 今天的日期
	 */
	private Calendar currentDay;
	
	/**
	 * 显示全部课程
	 */
	private boolean showAllLesson;
	
	/**
	 * 隐藏已结课程
	 */
	private boolean hideFinishLesson;
	
	/**
	 * 隐藏教师
	 */
	private boolean hideTeacher;
	
	private int baseLine;
	
	private int textHeight;
	
	/**
	 * 左侧时间和顶部日期的宽度
	 */
	public int timeWidth, dateHeight;
	
	/**
	 * 最小的一节课的大小
	 */
	private int cellWidth, cellHeight;
	
	/**
	 * 不同文本之间的间距
	 */
	private int linePadding;
	
	private Paint paint, paintT;
	
	
	private String[][] timeText;
	
	private  LessonRenderData lessonRenderData;
	
	public LessonRender(@NonNull Context context){
		currentDay = Calendar.getInstance();
		
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3);
		
		paintT = new Paint();
		paintT.setDither(true);
		paintT.setAntiAlias(true);
		paintT.setSubpixelText(true);
		paintT.setTextAlign(Paint.Align.CENTER);
		
		paintT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
		
		baseLine = (int)(paintT.getTextSize() / 2 + (paintT.getFontMetrics().descent - paintT.getFontMetrics().ascent) / 2 - paintT.getFontMetrics().descent);
		
		textHeight = (int)(paintT.getTextSize() + 3);
		
		timeWidth = ParamUtil.dp2px(context, 48);
		linePadding = ParamUtil.dp2px(context, 4);
		
		dateHeight = 40 + textHeight * 2;
		
		timeText = LessonTableViewModel.getInstance(context).getLessonTimeText();
		
		hideTeacher = SettingUtil.getBoolean(context.getString(R.string.KEY_HIDE_TEACHER), false);
		showAllLesson = SettingUtil.getBoolean(context.getString(R.string.KEY_SHOW_ALL_LESSON), false);
		hideFinishLesson = SettingUtil.getBoolean(context.getString(R.string.KEY_HIDE_FINISH_LESSON), false);
		
		lessonRenderData = new LessonRenderData(hideTeacher, paintT);
	}
	
	
	/**
	 * 设置 / 更新 课表信息
	 * @param lessonTable 课表信息
	 */
	public void setLessonTable(@NonNull LessonTable lessonTable){
		lessonRenderData.setLessonTable(lessonTable);
		if(cellWidth != 0 && cellHeight != 0){
			lessonRenderData.setMeasureData(cellWidth - (LESSON_PADDING << 2));
			lessonRenderData.calcLessonData();
		}
	}
	
	/**
	 * 设置 View Measure 数据
	 * 必须调用，不然无法显示
	 */
	public void setMeasureData(int measuredWidth, int measuredHeight){
		cellWidth = (measuredWidth - timeWidth) / WEEK_STRING.length;
		cellHeight = (measuredHeight - dateHeight) / timeText[0].length;
		lessonRenderData.setMeasureData(cellWidth - (LESSON_PADDING << 2));
		lessonRenderData.calcLessonData();
	}
	
	/**
	 * 获取点击位置的课程
	 * @param week 当前周
	 * @param downX 点击X坐标
	 * @param downY 点击Y坐标
	 * @param result 存放计算出的点击位置
	 * @return result 里为 [dayOfWeek, timeSlot]， return 值为点击到的课程
	 */
	@Nullable
	public Lesson getClickLesson(int week, int downX, int downY, int[] result){
		if(downX < timeWidth || downY < dateHeight){
			result[0] = -1;
			result[1] = -1;
			return null;
		}
		
		// 计算点击的位置是星期几
		int dayOfWeek = (downX - timeWidth) / cellWidth;
		
		if(dayOfWeek >= WEEK_STRING.length){
			result[0] = -1;
			result[1] = -1;
			return null;
		}
		
		result[0] = dayOfWeek;
		
		int y = downY - dateHeight;
		
		LessonRenderData.LessonHolder holder;
		LessonRenderData.LessonData lessonData;
		
		for(int timeSlot = 0; timeSlot < lessonRenderData.lessons[dayOfWeek].length; timeSlot++, y -= cellHeight){
			holder = lessonRenderData.lessons[dayOfWeek][timeSlot];
			
			if(holder != null){
				lessonData = holder.current(week);
				
				if(lessonData == null && showAllLesson){
					lessonData = holder.findLesson(week, !hideFinishLesson);
				}
				
				if(lessonData != null){
					if(y < lessonData.len * cellHeight){
						result[1] = timeSlot;
						return lessonRenderData.getLessonByHolder(week, dayOfWeek, timeSlot);
					}
				}
			}
			
			if(y < cellHeight){
				result[1] = timeSlot;
				return null;
			}
		}
		
		result[0] = -1;
		result[1] = -1;
		return null;
	}
	
	public boolean hasNextLesson(int week, int dayOfWeek, int timeSlot){
		return lessonRenderData.lessons[dayOfWeek][timeSlot].hasNext(week);
	}
	
	public Lesson nextLesson(int week, int dayOfWeek, int timeSlot){
		lessonRenderData.lessons[dayOfWeek][timeSlot].next(week);
		return lessonRenderData.getLessonByHolder(week, dayOfWeek, timeSlot);
	}
	
	/**
	 * 绘制View
	 * @param week 绘制第几周，从0开始
	 */
	public void drawView(Canvas canvas, int week){
		drawTime(canvas);
		drawDate(canvas, week);
		drawLessons(canvas, week);
	}
	
	/**
	 * 绘制选中高亮框
	 * @param week 星期几
	 * @param count 第几节
	 * @param len 课程长度
	 */
	public void drawHighlightBox(Canvas canvas, int week, int count, int len){
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.rgb(0, 176, 255));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			canvas.drawRoundRect(week * cellWidth + timeWidth + LESSON_PADDING, count * cellHeight + dateHeight + LESSON_PADDING, week * cellWidth + cellWidth + timeWidth - LESSON_PADDING, count * cellHeight + cellHeight * len + dateHeight - LESSON_PADDING, 16, 16, paint);
		}else{
			canvas.drawRoundRect(new RectF(week * cellWidth + timeWidth + LESSON_PADDING, count * cellHeight + dateHeight + LESSON_PADDING, week * cellWidth + cellWidth + timeWidth - LESSON_PADDING, count * cellHeight + cellHeight * len + dateHeight - LESSON_PADDING), 16, 16, paint);
		}
		paint.setStyle(Paint.Style.FILL);
	}
	
	protected void drawTime(Canvas canvas){
		paintT.setColor(Color.GRAY);
		int x = timeWidth / 2;
		int y = dateHeight + baseLine + (cellHeight - textHeight * 2) / 2;
		for(int i = 0; i < lessonRenderData.lessons[0].length; i++){
			canvas.drawText(timeText[0][i], x, y, paintT);
			canvas.drawText(timeText[1][i], x, y + textHeight, paintT);
			y += cellHeight;
		}
	}
	
	protected void drawDate(Canvas canvas, int week){
		Calendar c = (Calendar) lessonRenderData.startDate.clone();
		
		c.add(Calendar.WEEK_OF_YEAR, week);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
		c.add(Calendar.DATE, -dayOfWeek);
		
		int y = 20 + baseLine;
		
		for(int i = 0; i < WEEK_STRING.length; i++){
			
			if(currentDay.get(Calendar.DATE) == c.get(Calendar.DATE) && currentDay.get(Calendar.MONTH) == c.get(Calendar.MONTH)){
				paintT.setColor(ColorUtil.TEXT_COLORS[0]);
			}else{
				paintT.setColor(Color.GRAY);
			}
			
			String day = DateUtil.MD.format(c.getTime());
			
			canvas.drawText(WEEK_STRING[i], timeWidth + cellWidth / 2 + i * cellWidth, y, paintT);
			canvas.drawText(day, timeWidth + cellWidth / 2 + i * cellWidth, y + textHeight, paintT);
			
			c.add(Calendar.DATE, 1);
		}
	}
	
	protected void drawLessons(Canvas canvas, int week){
		int x = timeWidth;
		
		boolean colorFlag;
		
		for(int i = 0; i < lessonRenderData.lessons.length; i++, x += cellWidth){
			
			int y = dateHeight;
			
			for(int j = 0; j < lessonRenderData.lessons[0].length; j++, y += cellHeight){
				
				LessonRenderData.LessonHolder holder = lessonRenderData.lessons[i][j];
				
				if(holder == null) continue;
				
				LessonRenderData.LessonData lesson = holder.current(week);
				
				if(lesson == null){
					if(!showAllLesson) continue;
					lesson = holder.findLesson(week, !hideFinishLesson);
					if(lesson == null) continue;
					colorFlag = false;
				}else{
					colorFlag = true;
				}
				
				if(colorFlag){
					paint.setColor(ColorUtil.BACKGROUND_COLORS[lesson.color]);
					paintT.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
				}else{
					paint.setColor(ColorUtil.BACKGROUND_COLOR_SECOND);
					paintT.setColor(ColorUtil.TEXT_COLOR_SECOND);
				}
				
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
					canvas.drawRoundRect(x + LESSON_PADDING, y + LESSON_PADDING, x + cellWidth - LESSON_PADDING, y + cellHeight * lesson.len - LESSON_PADDING, 16, 16, paint);
				}else{
					canvas.drawRoundRect(new RectF(x + LESSON_PADDING, y + LESSON_PADDING, x + cellWidth - LESSON_PADDING, y + cellHeight * lesson.len - LESSON_PADDING), 16, 16, paint);
				}
				
				canvas.drawText(lesson.type == 0 ? "A" : "U", x + (LESSON_PADDING << 2) + 3, y + baseLine + (LESSON_PADDING << 2), paintT);
				
				if(holder.count[week] > 1){
					canvas.drawText((holder.index[week] + 1) + "/" + holder.count[week], x + cellWidth / 2, y + cellHeight * lesson.len - textHeight + baseLine - (LESSON_PADDING << 2), paintT);
				}
				
				int lineY = y + baseLine + (cellHeight * lesson.len - textHeight * lesson.lines - linePadding * (hideTeacher ? 1 : 2)) / 2;
				
				int c = lesson.data.length;
				for(int n = 0; n < c; n++){
					if(lesson.data[n] == null){
						lineY += linePadding;
					}else {
						canvas.drawText(lesson.data[n], x + cellWidth / 2, lineY, paintT);
						lineY += textHeight;
					}
				}
			}
		}
	}
	
}
