package com.university.assistant.fragment.lessontable;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.MingDeUtil;
import com.university.assistant.widget.BackgroundLesson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

import androidx.annotation.Nullable;

public class Lesson implements Serializable{
	
	public static final String[] LESSON_TIME_WINTER = new String[]{
			"08:00\n09:50",
			"08:00\n09:50",
			"10:10\n12:00",
			"13:30\n15:20",
			"15:40\n17:30",
			"18:00\n19:50"
	};
	
	public static final String[] LESSON_TIME_SUMMER = new String[]{
			"08:00\n09:50",
			"08:00\n09:50",
			"10:10\n12:00",
			"14:00\n15:50",
			"16:10\n18:00",
			"18:30\n20:20"
	};
	
	public static final Lesson EMPTY_LESSON = new Lesson();
	
	// 星期几
	public int week;
	// 节次
	public int count;
	
	public BaseLesson[] lessons;

	public Lesson(){
		lessons = new BaseLesson[0];
	}
	
	// 添加一节课程
	public void addLesson(BaseLesson lesson){
		lessons = Arrays.copyOf(lessons,lessons.length + 1);
		lessons[lessons.length - 1] = lesson;
	}
	
	public static Lesson getLesson(Lesson lesson,int week){
		if(lesson == null) return EMPTY_LESSON;
		BaseLesson baseLesson = lesson.getCurrentLesson(week);
		return baseLesson == null ? EMPTY_LESSON : lesson;
	}
	
	// 解析js为lesson对象
	public static BaseLesson getLesson(JSONObject json){
		BaseLesson lesson = new BaseLesson();
		try{
			String zcd = json.getString("zcd");
			String[] sp = zcd.split(",");
			for(String a:sp){
				int type = -1;
				if(a.endsWith(")")){
					type = a.charAt(a.length()-2)=='单' ? 1 : 0;
					a = a.substring(0,a.length()-4);
				}else a = a.substring(0,a.length()-1);
				if(a.contains("-")){
					String[] p = a.split("-");
					lesson.fillLesson(true,Integer.parseInt(p[0]),Integer.parseInt(p[1]),type);
				}else{
					lesson.week[Integer.parseInt(a)] = true;
				}
			}
			
			lesson.place = json.getString("cdmc");
			
			lesson.name = json.getString("kcmc");
			
			lesson.teacher = json.getString("xm");
			
		}catch(JSONException e){
			LogUtil.Log(e);
		}
		return lesson;
	}
	
	/*
	 * 获取当前周的课程
	 */
	@Nullable
	public BaseLesson getCurrentLesson(int week){
		for(BaseLesson lesson:lessons){
			if(lesson.week[week - 1])return lesson;
		}
		return null;
	}
	
	// 查找最接近当前周的课程，阿巴阿巴，说不明白
	@Nullable
	public BaseLesson findLesson(int week){
		for(int i=week;i>0;i--){
			for(BaseLesson lesson : lessons){
				if(lesson.week[i - 1]){
					return lesson;
				}
			}
		}
		for(int i=0;i<lessons[0].week.length;i++){
			for(BaseLesson lesson : lessons){
				if(lesson.week[i]){
					return lesson;
				}
			}
		}
		return null;
	}
	
	public View getView(Context context,int hour,int minute){
		View v = LayoutInflater.from(context).inflate(R.layout.item_lesson,null);
		((TextView)v.findViewById(R.id.item_lesson_time)).setText(LESSON_TIME_SUMMER[count]);
		TextView n = v.findViewById(R.id.item_lesson_name);
		BaseLesson lesson = getCurrentLesson(LessonTableData.getInstance().getCurrentWeek());
		if(lesson == null){
			n.setText("空闲");
			n.setTextColor(Color.GRAY);
		}else{
			((BackgroundLesson)v.findViewById(R.id.item_lesson_color)).setColor(ColorUtil.TEXT_COLORS[lesson.color]);
			n.setText(lesson.name);
			n.setTextColor(Color.BLACK);
			if("".equals(lesson.place) || "".equals(lesson.teacher))
				((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place + lesson.teacher);
			else ((TextView)v.findViewById(R.id.item_lesson_info)).setText(lesson.place +" | " + lesson.teacher);
//			TextView t = v.findViewById(R.id.item_lesson_status);
//			if(hour>1){
//				t.setText("已结束");
//			}else if(hour==1){
//				if(minute>50){
//					t.setText("已结束");
//				}else{
//					t.setText((50 - minute) + "min后下课");
//					t.setTextColor(ColorUtil.TEXT_COLORS[1]);
//					n.getPaint().setFakeBoldText(true);
//				}
//			}else if(hour==0){
//				if(minute>50){
//					t.setText((110 - minute) + "min后下课");
//				}else t.setText("1h" + (50 - minute) + "min后下课");
//				t.setTextColor(ColorUtil.TEXT_COLORS[1]);
//				n.getPaint().setFakeBoldText(true);
//			}else if(hour<0){
//				t.setText("未开始");
//			}
		}
		return v;
	}
	
	public boolean isCurrentLesson(int hour,int minute){
		if(hour>1) return false;
		else if(hour==1) return minute<=50;
		else return hour==0;
	}
	
	public static class BaseLesson{
		// 课程颜色
		public int color;
		// 课程课时
		public int len;
		// 第几周有课
		public boolean[] week;
		// 教室
		public String place;
		// 名称
		public String name;
		// 教师
		public String teacher;
		
		public BaseLesson(){
			week = new boolean[30];
			place = "";
			name = "";
			teacher = "";
		}
		
		// 填充课程，type -1 正常 1 单周 0 双周
		public void fillLesson(boolean b, int start, int end, int type){
			if(type == -1){
				Arrays.fill(week,start - 1, end, b);
			}else{
				for(int i=start-1;i<end;i++){
					if(i%2 == type)week[i] = b;
				}
			}
		}
	}
	
}
