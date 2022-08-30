package com.qust.assistant.lesson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.widget.BackgroundLesson;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 课程组
 * 表示N节不同的课程
 */
public class LessonGroup implements Serializable, Cloneable{
	
	public static final LessonGroup EMPTY_LESSON_GROUP = new LessonGroup(0, 0);
	
	private static final long serialVersionUID = -508836842701328284L;
	
	// 星期几 1-7
	public int week;
	// 节次 1-10
	public int count;
	
	public Lesson[] lessons;
	
	public LessonGroup(int _week, int _count){
		week = _week;
		count = _count;
		lessons = new Lesson[0];
	}
	
	// 添加一节课程
	public void addLesson(Lesson lesson){
		lessons = Arrays.copyOf(lessons, lessons.length + 1);
		lessons[lessons.length - 1] = lesson;
	}
	
	public void removeLesson(Lesson l){
		Lesson[] tmp = new Lesson[lessons.length - 1];
		int index = 0;
		for(Lesson lesson : lessons){
			if(lesson.equals(l)) continue;
			tmp[index++] = lesson;
		}
		lessons = tmp;
	}
	
	public void removeLesson(int index){
		Lesson[] l = new Lesson[lessons.length - 1];
		int j = 0;
		for(int i = 0; i < lessons.length; i++){
			if(i == index) continue;
			l[j++] = lessons[i];
		}
		lessons = l;
	}
	
	/**
	 * 获取 {@code week} 周会上的课程
	 *
	 * @param week 第 n 周
	 *
	 * @return 会上的课程, 没有课则为null
	 */
	@Nullable
	public Lesson getCurrentLesson(int week){
		for(Lesson lesson : lessons){
			if(week <= lesson.week.length && lesson.week[week - 1]) return lesson;
		}
		return null;
	}
	
	/**
	 * 查找最接近 {@code week} 周会上的课程
	 *
	 * @param week 第 n 周
	 * @param findAll 查找全部时间
	 */
	@Nullable
	public Lesson findLesson(int week, boolean findAll){
		if(lessons.length == 0) return null;
		
		// 向后查找课程
		int len = lessons[0].week.length;
		for(int i = week + 1; i < len; i++){
			for(Lesson lesson : lessons){
				if(i < lesson.week.length && lesson.week[i]){
					return lesson;
				}
			}
		}
		
		// 向前查找课程
		if(findAll){
			for(int i = week - 1; i >= 0; i--){
				for(Lesson lesson : lessons){
					if(lesson.week[i]){
						return lesson;
					}
				}
			}
		}
		return null;
	}
	
	
	public static LessonGroup getLesson(LessonGroup lessonGroup, int week){
		if(lessonGroup == null) return EMPTY_LESSON_GROUP;
		Lesson baseLesson = lessonGroup.getCurrentLesson(week);
		return baseLesson == null ? EMPTY_LESSON_GROUP : lessonGroup;
	}
	
	/**
	 * 获取用于展示的View
	 */
	public static View getView(Context context, Lesson lesson, int count, int len){
		View v = LayoutInflater.from(context).inflate(R.layout.item_lesson, null);
		// 设置课程时间文本
		((TextView)v.findViewById(R.id.item_lesson_time)).setText(LessonTableViewModel.getLessonTimeText()[0][count] + "\n" + LessonTableViewModel.getLessonTimeText()[1][count + len - 1]);
		TextView n = v.findViewById(R.id.item_lesson_name);
		if(lesson == null){
			n.setText("空闲");
			n.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
		}else{
			((BackgroundLesson)v.findViewById(R.id.item_lesson_color)).setColor(ColorUtil.TEXT_COLORS[lesson.color]);
			n.setText(lesson.name);
			n.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
			if("".equals(lesson.place) || "".equals(lesson.teacher))
				((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place + lesson.teacher);
			else
				((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place + " | " + lesson.teacher);
		}
		return v;
	}
	
	@NonNull
	@Override
	public LessonGroup clone(){
		LessonGroup b = new LessonGroup(week, count);
		b.lessons = lessons.clone();
		return b;
	}
}
