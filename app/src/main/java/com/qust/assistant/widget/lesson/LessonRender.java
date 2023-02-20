package com.qust.assistant.widget.lesson;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.lesson.Lesson;
import com.qust.assistant.model.lesson.LessonGroup;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.SettingUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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
	 * 开学日期
	 */
	private Calendar startDate;
	
	/**
	 * 今天的日期
	 */
	private Calendar currentDay;
	
	/**
	 * 总周数
	 */
	private int totalWeeks;
	
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
	
	private LessonCell[][][] lessons;
	
	
	public LessonRender(@NonNull Context context){
		
		startDate = Calendar.getInstance();
		
		currentDay = Calendar.getInstance();
		
		totalWeeks = LessonTableViewModel.getTotalWeek();
		
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
		
		hideTeacher = SettingUtil.getBoolean(SettingUtil.KEY_HIDE_TEACHER, false);
		showAllLesson = SettingUtil.getBoolean(SettingUtil.KEY_SHOW_ALL_LESSON, false);
		hideFinishLesson = SettingUtil.getBoolean(SettingUtil.KEY_HIDE_FINISH_LESSON, false);
	}
	
	
	/**
	 * 设置学期信息
	 * @param _startDate 开学时间
	 * @param _totalWeeks 总周数
	 */
	public void setTermData(String _startDate, int _totalWeeks){
		totalWeeks = _totalWeeks;
		
		try{
			Date date = DateUtil.YMD.parse(_startDate);
			if(date != null) startDate.setTime(date);
		}catch(ParseException ignored){
		}
	}
	
	/**
	 * 设置 / 更新 课表信息，需要在 {@link LessonRender#setMeasureData} 之后调用
	 * @param lessonGroups 课表信息
	 */
	public void setLessonData(@NonNull LessonGroup[][] lessonGroups){
		lessons = new LessonCell[totalWeeks][lessonGroups.length][lessonGroups[0].length];
		
		for(int week = 0; week < totalWeeks; week++){
			for(int i = 0; i < lessonGroups.length; i++){
				int j = 0;
				while(j < lessonGroups[0].length){
					LessonGroup lessonGroup = lessonGroups[i][j];
					if(lessonGroup != null){
						Lesson lesson = lessonGroup.getCurrentLesson(week + 1);
						if(lesson != null){
							lessons[week][i][j] = new LessonCell(lesson.color, lesson);
							j += lesson.len;
							continue;
						}
					}
					j++;
				}
			}
		}
		
		if(showAllLesson){
			for(int week = 0; week < totalWeeks; week++){
				for(int i = 0; i < lessonGroups.length; i++){
					int j = 0;
					while(j < lessonGroups[0].length){
						LessonCell lessonCell = lessons[week][i][j];
						if(lessonCell != null){
							j += lessonCell.len;
							continue;
						}
						LessonGroup lessonGroup = lessonGroups[i][j];
						if(lessonGroup != null){
							// 本周该时间无课但是其他周有课
							Lesson lesson = lessonGroup.findLesson(week + 1, !hideFinishLesson);
							if(lesson != null){
								boolean conflictFlag = false;
								for(int p = j + 1, l = 0; p < lessonGroups[0].length && l < lesson.len - 1; p++, l++){
									// 向下检查是否有冲突
									if(lessons[week][i][p] != null){
										conflictFlag = true;
										break;
									}
								}
								if(!conflictFlag){
									lessons[week][i][j] = new LessonCell(-1, lesson);
									j += lesson.len;
									continue;
								}
							}
						}
						j++;
					}
				}
			}
		}
	}
	
	/**
	 * 设置 View Measure 数据
	 */
	public void setMeasureData(int measuredWidth, int measuredHeight){
		cellWidth = (measuredWidth - timeWidth) / WEEK_STRING.length;
		cellHeight = (measuredHeight - dateHeight) / LessonTableViewModel.getLessonTimeText()[0].length;
	}
	
	/**
	 * 获取点击位置的课程
	 * @return [week, count, 是否有课(0为无课)]
	 */
	public int[] getClickLesson(int showWeek, int downX, int downY){
		if(downX < timeWidth || downY < dateHeight) return new int[] { -1, -1 , 0 };
		
		int y = downY - dateHeight;
		
		// 计算点击的位置在第几周
		int currentWeek = (downX - timeWidth) / cellWidth;
		
		for(int i = 0; i < lessons[showWeek][currentWeek].length; i++){
			
			LessonCell lesson = lessons[showWeek][currentWeek][i];
			
			if(lesson != null && y < lesson.len * cellHeight){
				return new int[] { currentWeek, i , 1 };
			}
			
			if(y < cellHeight){
				return new int[] { currentWeek, i , 0 };
			}
			
			y -= cellHeight;
		}
		
		return new int[] { -1, -1, 0 };
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
		String[][] timeText = LessonTableViewModel.getLessonTimeText();
		int x = timeWidth / 2;
		int y = dateHeight + baseLine + (cellHeight - textHeight * 2) / 2;
		for(int i = 0; i < lessons[0][0].length; i++){
			canvas.drawText(timeText[0][i], x, y, paintT);
			canvas.drawText(timeText[1][i], x, y + textHeight, paintT);
			y += cellHeight;
		}
	}
	
	protected void drawDate(Canvas canvas, int week){
		
		Calendar c = (Calendar) startDate.clone();
		
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
	
	protected void drawLessons(Canvas canvas, int showWeek){
		int x = timeWidth;
		
		for(int i = 0; i < lessons[showWeek].length; i++, x += cellWidth){
			
			int y = dateHeight;
			
			for(int j = 0; j < lessons[showWeek][0].length; j++, y += cellHeight){
				
				LessonCell lesson = lessons[showWeek][i][j];
				
				if(lesson == null) continue;
				
				paint.setColor(lesson.color == -1 ? ColorUtil.BACKGROUND_COLOR_SECOND : ColorUtil.BACKGROUND_COLORS[lesson.color]);
				paintT.setColor(lesson.color == -1 ? ColorUtil.TEXT_COLOR_SECOND : ColorUtil.TEXT_COLORS[lesson.color]);
				
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
					canvas.drawRoundRect(x + LESSON_PADDING, y + LESSON_PADDING, x + cellWidth - LESSON_PADDING, y + cellHeight * lesson.len - LESSON_PADDING, 16, 16, paint);
				}else{
					canvas.drawRoundRect(new RectF(x + LESSON_PADDING, y + LESSON_PADDING, x + cellWidth - LESSON_PADDING, y + cellHeight * lesson.len - LESSON_PADDING), 16, 16, paint);
				}
				
				int lineY = y + baseLine + (cellHeight * lesson.len - textHeight * lesson.lines - linePadding * (hideTeacher ? 1 : 2)) / 2;
				
				int c = lesson.lines + (hideTeacher ? 1 : 2);
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

	/**
	 * 字符串分行
	 * @param src 要分行的字符串
	 * @param des 分行后的字符串
	 * @param maxWidth 一行最大的长度
	 * @param desPos 目标数组开始的位置
	 * @param maxLine 最大支持的行数
	 */
	private int splitString(@NonNull String src, String[] des, int maxWidth, int desPos, int maxLine){
		int srcPos = 0;
		int lines = 0;
		int length = src.length();
		for(int i = srcPos; i < length;){
			i = srcPos + paintT.breakText(src, i, length, true, maxWidth, null);
			des[desPos + lines] = src.substring(srcPos, i);
			srcPos = i;
			if(lines++ == maxLine){
				lines--;
				return lines;
			}
		}
		return lines;
	}
	
	
	private class LessonCell{
		/**
		 * 课程长度
		 */
		public int len;
		
		/**
		 * 课程颜色
		 */
		public int color;
		
		/**
		 * 文本信息的行数
		 */
		public int lines;
		
		/**
		 * 课程文本信息
		 */
		public String[] data;
		
		public LessonCell(int color, @NonNull Lesson lesson){
			this.color = color;
			this.len = lesson.len;

			data = new String[len * 3 + (hideTeacher ? 1 : 2)];
			
			lines = splitString(lesson.name, data, cellWidth - (LESSON_PADDING << 2), 0, len * 3 - 2);
			lines += splitString(lesson.place, data, cellWidth - (LESSON_PADDING << 2), lines + 1, len * 3 - lines - 1);
			if(!hideTeacher) lines += splitString(lesson.teacher, data, cellWidth - (LESSON_PADDING << 2), lines + 2, len * 3 - lines);
		}
	}
	
}
