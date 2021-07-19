package com.university.assistant.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.fragment.lessontable.Lesson;
import com.university.assistant.fragment.lessontable.LessonTableData;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.widget.BackgroundLesson;

import java.util.Calendar;

public class LessonTable extends AppWidgetProvider{
	
	private static int[][] ids = {
			{R.id.widget_lesson_1,R.id.widget_lesson_1_color,R.id.widget_lesson_1_name,R.id.widget_lesson_1_info,R.id.widget_lesson_1_status},
			{R.id.widget_lesson_2,R.id.widget_lesson_2_color,R.id.widget_lesson_2_name,R.id.widget_lesson_2_info,R.id.widget_lesson_2_status},
			{R.id.widget_lesson_3,R.id.widget_lesson_3_color,R.id.widget_lesson_3_name,R.id.widget_lesson_3_info,R.id.widget_lesson_3_status},
			{R.id.widget_lesson_4,R.id.widget_lesson_4_color,R.id.widget_lesson_4_name,R.id.widget_lesson_4_info,R.id.widget_lesson_4_status},
			{R.id.widget_lesson_5,R.id.widget_lesson_5_color,R.id.widget_lesson_5_name,R.id.widget_lesson_5_info,R.id.widget_lesson_5_status},
	};
	
	private RemoteViews remoteView;
	
	private int times;
	
	// 每次窗口小部件被更新都调用一次该方法
	@Override
	public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds){
		super.onUpdate(context,appWidgetManager,appWidgetIds);
		updateWidget(context,appWidgetManager,appWidgetIds);
	}
	
	// 当小部件大小改变时
	@Override
	public void onAppWidgetOptionsChanged(Context context,AppWidgetManager appWidgetManager,int appWidgetId,Bundle newOptions){
		super.onAppWidgetOptionsChanged(context,appWidgetManager,appWidgetId,newOptions);
		updateWidget(context,appWidgetManager,appWidgetId);
	}
	
	private void updateWidget(Context context,AppWidgetManager appWidgetManager,int... appWidgetIds){
		
		LessonTableData.init(context);
		
		Lesson[] lessons = LessonTableData.getInstance().getLessons()[LessonTableData.getInstance().getWeek()];
		
		int currentWeek = LessonTableData.getInstance().getCurrentWeek();
		
		if(remoteView==null){
			remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_lesson);
			times = 0;
		}
		
		if(times--==0){
			LessonTableData.getInstance().updateDay();
			times = 200;
			
			Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);

//			for(int i=0;i<5;i++){
//				createLesson(remoteView,Lesson.getLesson(lessons[i],currentWeek,i+1),paint,i,i%2!=0);
//			}
		}
		
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		for(int i=0;i<5;i++){
			h -= LessonTableData.summer[i][0];
			m -= LessonTableData.summer[i][1];
			if(m<0){
				h -= 1;
				m += 60;
			}
//			updateTime(remoteView,Lesson.getLesson(lessons[i],currentWeek,i+1),i,h,m);
		}
		
		for(int appWidgetId:appWidgetIds)appWidgetManager.updateAppWidget(appWidgetId,remoteView);
		
	}
	
	private void createLesson(RemoteViews remoteViews,Lesson lesson,Paint paint,int index,boolean canHide){
//		if("".equals(lesson.name)){
//			if(canHide){
//				remoteViews.setViewVisibility(ids[index][0],View.GONE);
//			}else{
//				remoteViews.setTextViewText(ids[index][2],"空闲");
//				remoteViews.setTextColor(ids[index][2],Color.GRAY);
//			}
//		}else{
//			paint.setColor(ColorUtil.TEXT_COLORS[lesson.color]);
//			remoteViews.setViewVisibility(ids[index][0],View.VISIBLE);
//			remoteViews.setImageViewBitmap(ids[index][1],getColorBitmap(paint));
//			remoteViews.setTextViewText(ids[index][2],lesson.name);
//			remoteViews.setTextColor(ids[index][2],Color.BLACK);
//			if("".equals(lesson.place) || "".equals(lesson.teacher))
//				remoteViews.setTextViewText(ids[index][3],lesson.place + lesson.teacher);
//			else remoteViews.setTextViewText(ids[index][3],lesson.place + "|" + lesson.teacher);
//
//		}
	}
	
	private void updateTime(RemoteViews remoteViews,Lesson lesson,int index,int hour,int minute){
//		if("".equals(lesson.name)){
//			remoteViews.setTextViewText(ids[index][4],"");
//			return;
//		}
//		if(hour>1){
//			remoteViews.setTextViewText(ids[index][4],"已结束");
//		}else if(hour==1){
//			if(minute>50){
//				remoteViews.setTextViewText(ids[index][4],"已结束");
//			}else{
//				remoteViews.setTextViewText(ids[index][4],(50 - minute) + "min后下课");
//				remoteViews.setTextColor(ids[index][4],ColorUtil.TEXT_COLORS[1]);
//				//.getPaint().setFakeBoldText(true);
//			}
//		}else if(hour==0){
//			remoteViews.setTextColor(ids[index][4],ColorUtil.TEXT_COLORS[1]);
//			if(minute>50) remoteViews.setTextViewText(ids[index][4],(110 - minute) + "min后下课");
//			else remoteViews.setTextViewText(ids[index][4],"1h" + (50 - minute) + "min后下课");
//			//n.getPaint().setFakeBoldText(true);
//		}else{
//			remoteViews.setTextViewText(ids[index][4],"未开始");
//		}
	}
	
	private Bitmap getColorBitmap(Paint paint){
		Bitmap bitmap = Bitmap.createBitmap(32,128,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRoundRect(new RectF(0,0,bitmap.getWidth(),bitmap.getHeight()),16,16,paint);
		return bitmap;
	}
	
	// 接收窗口小部件点击时发送的广播
	@Override
	public void onReceive(Context context,Intent intent){
		super.onReceive(context,intent);
	}
	
	// 每删除一次窗口小部件就调用一次
	@Override
	public void onDeleted(Context context,int[] appWidgetIds){
		super.onDeleted(context,appWidgetIds);
		//context.stopService(new Intent(context, WidgetService.class));
		//Log.i("AppWidget", "删除成功！");
	}
	
	// 当该窗口小部件第一次添加到桌面时调用该方法
	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
		remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_lesson);
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
	public void onRestored(Context context,int[] oldWidgetIds,int[] newWidgetIds){
		super.onRestored(context,oldWidgetIds,newWidgetIds);
		remoteView = new RemoteViews(context.getPackageName(),R.layout.widget_lesson);
	}
}
