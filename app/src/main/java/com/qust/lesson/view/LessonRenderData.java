package com.qust.lesson.view;

import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.lesson.Lesson;
import com.qust.lesson.LessonGroup;
import com.qust.lesson.LessonTable;

import java.util.Arrays;
import java.util.Calendar;

/**
 * 课程渲染信息
 */
public class LessonRenderData{
	
	/**
	 * 开学日期
	 */
	public Calendar startDate;
	
	public LessonHolder[][] lessons;
	
	private int cellWidth;
	
	private Paint textPaint;
	
	private LessonTable lessonTable;
	
	/**
	 * 隐藏教师
	 */
	private boolean hideTeacher;
	
	
	public LessonRenderData(boolean hideTeacher, Paint textPaint){
		this.hideTeacher = hideTeacher;
		this.textPaint = textPaint;
		startDate = Calendar.getInstance();
		lessons = new LessonHolder[7][10];
	}
	
	public void setLessonTable(LessonTable lessonTable){
		this.lessonTable = lessonTable;
	}
	
	public void setMeasureData(int cellWidth){
		this.cellWidth = cellWidth;
	}
	
	/**
	 * 计算绘制课程的信息
	 */
	public void calcLessonData(){
		if(lessonTable == null) return;
		
		startDate.setTime(lessonTable.getStartDay());
		
		LessonGroup[][] lessonGroups = lessonTable.getLessons();
		
		int totalWeek = lessonTable.getTotalWeek();
		
		lessons = new LessonHolder[lessonGroups.length][lessonGroups[0].length];
		
		for(int dayOfWeek = 0; dayOfWeek < lessonGroups.length; dayOfWeek++){
			for(int timeSlot = 0; timeSlot < lessonGroups[0].length; timeSlot++){
				LessonGroup lessonGroup = lessonGroups[dayOfWeek][timeSlot];
				if(lessonGroup == null) continue;
				LessonHolder holder = new LessonHolder(lessonGroup, totalWeek);
				lessons[dayOfWeek][timeSlot] = holder;
			}
		}
	}
	
	/**
	 * 从用于渲染的LessonHolder中获取课表中实际的Lesson
	 * @param dayOfWeek
	 * @param timeSlot
	 * @return
	 */
	public Lesson getLessonByHolder(int week, int dayOfWeek, int timeSlot){
		return lessonTable.getLessons()[dayOfWeek][timeSlot].lessons[lessons[dayOfWeek][timeSlot].index[week]];
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
			i = srcPos + textPaint.breakText(src, i, length, true, maxWidth, null);
			
			if(desPos + lines >= des.length) return lines;
			
			des[desPos + lines] = src.substring(srcPos, i);
			srcPos = i;
			
			if(lines++ == maxLine) return lines;
		}
		return lines;
	}
	
	/**
	 * 课程组
	 */
	public class LessonHolder{
		
		public int[] index;
		
		public int[] count;
		public int[] lessonTime;
		
		public LessonData[] lessonData;
		
		public LessonHolder(@NonNull LessonGroup lessonGroup, int totalWeek){
			Lesson[] group = lessonGroup.lessons;
			
			index = new int[totalWeek];
			count = new int[totalWeek];
			lessonTime = new int[totalWeek];
			lessonData = new LessonData[group.length];
			
			Arrays.fill(index, -1);
			
			for(int i = 0, offset = 1; i < lessonData.length; i++, offset <<= 1){
				Lesson lesson = group[i];
				lessonData[i] = new LessonData(lesson);
				long week = 1L;
				for(int j = 0; j < totalWeek; j++, week <<= 1){
					if((lesson.week & week) > 0){
						if(index[j] == -1) index[j] = i;
						lessonTime[j] |= offset;
						count[j]++;
					}
				}
			}
		}
		
		/**
		 * 是否有下一节课
		 */
		public boolean hasNext(int week){
			return count[week] > 1;
		}
		
		/**
		 * 显示下一节课
		 */
		public void next(int week){
			long offset = 1L << (index[week] + 1);
			for(int i = index[week] + 1; i < count[week]; i++, offset <<= 1){
				if((lessonTime[week] & offset) > 0){
					index[week] = i;
					return;
				}
			}
			index[week] = 0;
		}
		
		@Nullable
		public LessonData current(int week){
			if(count[week] == 0) return null;
			return lessonData[index[week]];
		}
		
		/**
		 * 查找最接近当前周会上的课程
		 *
		 * @param week 当前周
		 * @param findAll 查找全部时间
		 */
		@Nullable
		public LessonData findLesson(int week, boolean findAll){
			if(index[week] != -1) return lessonData[index[week]];
			
			// 向后查找课程
			int i = week + 1;
			for(; i < count.length; i++){
				if(lessonTime[i] > 0){
					int pos = Long.numberOfTrailingZeros(lessonTime[i]);
					index[week] = pos;
					return lessonData[pos];
				}
			}
			
			// 向前查找课程
			if(findAll && week > 0){
				i = week - 1;
				for(; i >= 0; i--){
					if(lessonTime[i] > 0){
						int pos = Long.numberOfTrailingZeros(lessonTime[i]);
						index[week] = pos;
						return lessonData[pos];
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * 课程信息
	 */
	public class LessonData{
		
		/**
		 * 课程类型
		 * 0: auto 自动添加的课程
		 * 1: user 用户创建的课程
		 */
		public int type;
		
		/**
		 * 课程长度
		 */
		public int len;
		
		/**
		 * 课程颜色
		 */
		public int color;
		
		/**
		 * 上课周数
		 */
		public long week;
		
		/**
		 * 文本信息的行数
		 */
		public int lines;
		
		/**
		 * 课程文本信息
		 */
		public String[] data;
		
		public LessonData(@NonNull Lesson lesson){
			this.len = lesson.len;
			this.type = lesson.type;
			this.color = lesson.color;
			
			this.week = lesson.week;
			
			data = new String[len * 3 + (hideTeacher ? 1 : 2)];
			int l = data.length;
			
			lines = splitString(lesson.name, data, cellWidth, 0, l - 3);
			lines += splitString(lesson.place, data, cellWidth, lines + 1, l - lines - 1);
			
			if(!hideTeacher){
				lines += splitString(lesson.teacher, data, cellWidth, lines + 2, l - lines);
			}
		}
	}
}