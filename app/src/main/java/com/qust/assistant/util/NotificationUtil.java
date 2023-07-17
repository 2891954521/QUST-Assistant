package com.qust.assistant.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.qust.assistant.R;

public class NotificationUtil{
	
	private static final String NOTIFICATION_ID = "push";
	
	private static final String NOTIFICATION_NAME = "推送通知";
	
	private static final int NOTIFICATION_SHOW_AT_MOST = 10;
	
	private static int notificationNum = 0;
	
	public static void sendNotification(@NonNull Context context, String title, String content){
		
		if(notificationNum++ > NOTIFICATION_SHOW_AT_MOST) notificationNum = 0;
		
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// 检查渠道
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
			if(manager.getNotificationChannel(NOTIFICATION_ID) == null){
				NotificationChannel channel = new NotificationChannel(
						NOTIFICATION_ID,
						NOTIFICATION_NAME,
						NotificationManager.IMPORTANCE_HIGH
				);
				manager.createNotificationChannel(channel);
			}
		}
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_ID);
		
		builder.setAutoCancel(true)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
				.setWhen(System.currentTimeMillis())
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(Notification.PRIORITY_MAX)
				.setContentTitle(title)
				.setContentText(content);
		
		if(content.length() > 20){
			builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			builder.setColor(Color.TRANSPARENT)
					.setCategory(NotificationCompat.CATEGORY_MESSAGE)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//			.addAction(R.mipmap.ic_avatar, "去看看", pendingIntent)// 通知上的操作
		}

		Notification notification = builder.build();
		
//		Intent show = new Intent(Intent.ACTION_MAIN);
//		show.addCategory(Intent.CATEGORY_LAUNCHER);
//		show.setClass(app, PlayActivity.class);
//		show.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//		notification.contentIntent = PendingIntent.getActivity(app, 1, show, PendingIntent.FLAG_ONE_SHOT);
		
		manager.notify(notificationNum, notification);
	}
}
