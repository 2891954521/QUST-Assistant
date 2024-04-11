package com.qust.helper.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.qust.helper.R

object NotificationUtils {

	private const val NOTIFICATION_ID = "push"
	private const val NOTIFICATION_NAME = "推送通知"
	private const val NOTIFICATION_SHOW_AT_MOST = 10

	private var notificationNum = 0

	fun sendNotification(context: Context, title: String?, content: String) {
		if(notificationNum++ > NOTIFICATION_SHOW_AT_MOST) notificationNum = 0

		val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// 检查渠道
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
			if(manager.getNotificationChannel(NOTIFICATION_ID) == null) {
				val channel = NotificationChannel(
					NOTIFICATION_ID,
					NOTIFICATION_NAME,
					NotificationManager.IMPORTANCE_HIGH
				)
				manager.createNotificationChannel(channel)
			}
		}

		val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_ID)
			.setAutoCancel(true)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
			.setWhen(System.currentTimeMillis())
			.setDefaults(NotificationCompat.DEFAULT_ALL)
			.setPriority(Notification.PRIORITY_MAX)
			.setContentTitle(title)
			.setContentText(content)
			.setColor(Color.TRANSPARENT)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
		//	.addAction(R.mipmap.ic_avatar, "去看看", pendingIntent)

		if(content.length > 20) {
			builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
		}

		manager.notify(notificationNum, builder.build())
	}
}
