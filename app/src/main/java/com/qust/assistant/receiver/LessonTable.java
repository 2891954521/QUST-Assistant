package com.qust.assistant.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.util.ColorUtil;

import java.util.Calendar;

public class LessonTable extends AppWidgetProvider{
	
	private RemoteViews remoteView;
	
	private RemoteViews[] lessonView;
	
	private int times;
	
	// 每次窗口小部件被更新都调用一次该方法
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		updateWidget(context, appWidgetManager, appWidgetIds);
	}
	
	// 当小部件大小改变时
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions){
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		updateWidget(context, appWidgetManager, appWidgetId);
	}
	
	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int... appWidgetIds){
		
		LessonData.init(context);
		
		LessonGroup[] lessonGroups = LessonData.getInstance().getLessonGroups()[LessonData.getInstance().getWeek()];
		
		int currentWeek = LessonData.getInstance().getCurrentWeek();
		
		if(remoteView == null){
			remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_day_lesson);
			times = 0;
		}
		
		if(times-- == 0){
			LessonData.getInstance().updateDate();
			times = 200;
			
			Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
			
			String[] time = {"上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
			
			remoteView.removeAllViews(R.id.widget_day_lesson);
			
			lessonView = new RemoteViews[lessonGroups.length];
			
			for(int i = 0; i < lessonGroups.length; i++){
				if(time[i] != null){
					RemoteViews t = new RemoteViews(context.getPackageName(), R.layout.view_text);
					t.setTextViewText(R.id.view_text, time[i]);
					remoteView.addView(R.id.widget_day_lesson, t);
				}
				createLesson(context, remoteView, lessonGroups[i] == null ? null : lessonGroups[i].getCurrentLesson(currentWeek), paint, i, i % 4 != 0);
			}
		}
		
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		int pass = 0;
		int count = 0;
		
		boolean hasUpdateTime = false;
		
		Lesson lesson;
		
		for(int i = 0; i < lessonGroups.length; i++){
			if(pass == 0){
				if(lessonGroups[i] != null && (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) != null){
					count = i;
					pass = lesson.len;
					hasUpdateTime = false;
				}
			}
			
			h -= LessonData.Lesson_Time[i][0];
			m -= LessonData.Lesson_Time[i][1];
			if(m < 0){
				h -= 1;
				m += 60;
			}
			
			if(pass > 0){
				if(hasUpdateTime) continue;
				if(h > 0){
					if(pass == 1){
						lessonView[count].setTextViewText(R.id.widget_lesson_status, "已结束");
					}
				}else if(h == 0){
					if(m > 50){
						if(pass == 1){
							lessonView[count].setTextViewText(R.id.widget_lesson_status, "已结束");
						}
					}else{
						lessonView[count].setTextColor(R.id.widget_lesson_status, ColorUtil.TEXT_COLORS[0]);
						lessonView[count].setTextViewText(R.id.widget_lesson_status, (50 - m) + "min后下课");
						hasUpdateTime = true;
					}
				}else{
					lessonView[count].setTextViewText(R.id.widget_lesson_status, "未开始");
					hasUpdateTime = true;
				}
				pass--;
			}
		}
		
		for(int appWidgetId : appWidgetIds){
			appWidgetManager.updateAppWidget(appWidgetId, remoteView);
		}
		
	}
	
	private void createLesson(Context context, RemoteViews remoteViews, Lesson lesson, Paint paint, int count, boolean canHide){
		if(lesson == null && canHide) return;
		lessonView[count] = new RemoteViews(context.getPackageName(), R.layout.widget_lesson);
		RemoteViews view = lessonView[count];
		int len = 0;
		if(lesson == null){
			view.setTextViewText(R.id.widget_lesson_name, "空闲");
			view.setTextColor(R.id.widget_lesson_name, Color.GRAY);
		}else{
			len = lesson.len - 1;
			paint.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
			view.setImageViewBitmap(R.id.widget_lesson_color, getColorBitmap(paint));
			view.setTextViewText(R.id.widget_lesson_name, lesson.name);
			view.setTextColor(R.id.widget_lesson_name, context.getResources().getColor(R.color.colorPrimaryText));
			if("".equals(lesson.place) || "".equals(lesson.teacher))
				view.setTextViewText(R.id.widget_lesson_info, lesson.place + lesson.teacher);
			else view.setTextViewText(R.id.widget_lesson_info, lesson.place + "|" + lesson.teacher);
		}
		view.setTextViewText(R.id.widget_lesson_time, LessonData.Lesson_Time_Text[0][count] + "\n" + LessonData.Lesson_Time_Text[1][count + len]);
		remoteViews.addView(R.id.widget_day_lesson, view);
	}
	
	private Bitmap getColorBitmap(Paint paint){
		Bitmap bitmap = Bitmap.createBitmap(32, 128, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), 16, 16, paint);
		return bitmap;
	}
	
	// 接收窗口小部件点击时发送的广播
	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
	}
	
	// 每删除一次窗口小部件就调用一次
	@Override
	public void onDeleted(Context context, int[] appWidgetIds){
		super.onDeleted(context, appWidgetIds);
		//context.stopService(new Intent(context, WidgetService.class));
		//Log.i("AppWidget", "删除成功！");
	}
	
	// 当该窗口小部件第一次添加到桌面时调用该方法
	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
		remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_day_lesson);
		// Intent mTimerIntent = new Intent(context, WidgetService.class);
		// context.startService(mTimerIntent);
	}
	
	// 当最后一个该窗口小部件删除时调用该方法
	@Override
	public void onDisabled(Context context){
		super.onDisabled(context);
		//  Intent mTimerIntent = new Intent(context, WidgetService.class);
		// context.stopService(mTimerIntent);
	}
	
	// 当小部件从备份恢复时调用该方法
	@Override
	public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds){
		super.onRestored(context, oldWidgetIds, newWidgetIds);
		remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_day_lesson);
	}
}
