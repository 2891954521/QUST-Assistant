package com.qust.helper.ui.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.widget.RemoteViews
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.activity.ComposeActivity
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.widget.LessonTableView
import java.util.Calendar


class DailyLesson : AppWidgetProvider() {

	/**
	 * 小组件共用的View
	 */
	private var remoteView: RemoteViews? = null

	/**
	 * 存放每一节课程的数组
	 */
	private var lessonView: Array<RemoteViews> = emptyArray()

	private var times = 0

	private val TIME = arrayOf("上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null)

	private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, vararg appWidgetIds: Int) {
		val lessonGroups: Array<LessonGroup?> = LessonTableRepository.lessonTable.lessons[LessonTableRepository.dayOfWeek.intValue]
		val currentWeek: Int = LessonTableRepository.currentWeek.intValue

		val newRemoteView: RemoteViews
		if(remoteView != null) {
			newRemoteView = remoteView as RemoteViews
		}else{
			newRemoteView = RemoteViews(context.packageName, R.layout.widget_day_lesson)
			remoteView = newRemoteView
			times = 0
		}

		newRemoteView.setOnClickPendingIntent(R.id.widget_day_lesson, PendingIntent.getBroadcast(context, 0, Intent("DAILY_LESSON"), PendingIntent.FLAG_UPDATE_CURRENT))

		if(times-- <= 0) {
			// 清空所有课程重新添加
			newRemoteView.removeAllViews(R.id.widget_day_lesson)

			times = 200
			LessonTableRepository.updateDate()

			val paint = Paint(Paint.FILTER_BITMAP_FLAG).also { paint ->
				paint.isAntiAlias = true
				paint.style = Paint.Style.FILL
			}

			val lessons: ArrayList<RemoteViews> = ArrayList(lessonGroups.size)
			for(i in lessonGroups.indices) {
				if(TIME[i] != null) {
					RemoteViews(context.packageName, R.layout.view_text).let {
						it.setTextViewText(R.id.view_text, TIME[i])
						newRemoteView.addView(R.id.widget_day_lesson, it)
					}
				}
				val lesson = lessonGroups[i]?.getCurrentLesson(currentWeek)
				if(lesson != null){
					val view = createLesson(context, lesson, paint, i)
					newRemoteView.addView(R.id.widget_day_lesson, view)
					lessons.add(view)
				}else{
					if(i % 4 != 0) continue
					val view = RemoteViews(context.packageName, R.layout.widget_lesson)
					view.setTextViewText(R.id.widget_lesson_name, "空闲")
					view.setTextColor(R.id.widget_lesson_name, Color.GRAY)
					newRemoteView.addView(R.id.widget_day_lesson, view)
				}
			}
			lessonView = lessons.toTypedArray()
		}

		updateLessonTime(lessonGroups, currentWeek)

		appWidgetIds.forEach { appWidgetId ->
			appWidgetManager.updateAppWidget(appWidgetId, newRemoteView)
		}
	}

	private fun createLesson(context: Context, lesson: Lesson, paint: Paint, count: Int): RemoteViews {
		paint.color = TEXT_COLORS[lesson.color]
		val view = RemoteViews(context.packageName, R.layout.widget_lesson)
		view.setImageViewBitmap(R.id.widget_lesson_color, getColorBitmap(paint))
		view.setTextViewText(R.id.widget_lesson_name, lesson.name)
		view.setTextViewText(R.id.widget_lesson_info,  if(lesson.teacher.isEmpty() || lesson.place.isEmpty()) "${lesson.place}${lesson.teacher}" else "${lesson.place} | ${lesson.teacher}")
		view.setTextViewText(
			R.id.widget_lesson_time,
			if(LessonTableRepository.currentTimeTable == 0)
				"${LessonTableView.LESSON_TIME1[0][count]}\n${LessonTableView.LESSON_TIME1[1][count + lesson.len - 1]}"
			else
				"${LessonTableView.LESSON_TIME2[0][count]}\n${LessonTableView.LESSON_TIME2[1][count + lesson.len - 1]}"
		)
		return view
	}

	private fun updateLessonTime(lessonGroups: Array<LessonGroup?>, currentWeek: Int){
		val c = Calendar.getInstance()
		var h = c[Calendar.HOUR_OF_DAY] - 8
		var m = c[Calendar.MINUTE]
		var pass = 0
		var count = 0
		var hasUpdateTime = false
		var lesson: Lesson

		val timeTable = Data.LESSON_TIME[LessonTableRepository.currentTimeTable]
		for(i in lessonGroups.indices) {
			if(pass == 0) {
				lessonGroups[i]?.getCurrentLesson(currentWeek)?.let {
					lesson = it
					pass = lesson.len
					hasUpdateTime = false
				}
			}
			val delta: Int = timeTable[i]
			h -= delta / 60
			m -= delta % 60
			if(m < 0) {
				h -= 1
				m += 60
			}
			if(pass > 0) {
				if(hasUpdateTime) continue
				when{
					(h > 0) -> if(pass == 1) lessonView[count++].setTextViewText(R.id.widget_lesson_status, "已结束")

					(h == 0) ->
						if(m > 50) {
							if(pass == 1) lessonView[count++].setTextViewText(R.id.widget_lesson_status, "已结束")
						} else {
							lessonView[count].setTextColor(R.id.widget_lesson_status, TEXT_COLORS[0])
							lessonView[count++].setTextViewText(R.id.widget_lesson_status, (50 - m).toString() + "min后下课")
							hasUpdateTime = true
						}

					else -> {
						lessonView[count++].setTextViewText(R.id.widget_lesson_status, "未开始")
						hasUpdateTime = true
					}
				}
				pass--
			}
		}
	}

	private fun getColorBitmap(paint: Paint): Bitmap {
		val bitmap = Bitmap.createBitmap(32, 128, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		canvas.drawRoundRect(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), 16f, 16f, paint)
		return bitmap
	}

	// 每次窗口小部件被更新都调用一次该方法
	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
		super.onUpdate(context, appWidgetManager, appWidgetIds)
		updateWidget(context, appWidgetManager, *appWidgetIds)
	}

	// 当小部件大小改变时
	override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
		updateWidget(context, appWidgetManager, appWidgetId)
	}

	// 接收窗口小部件点击时发送的广播
	override fun onReceive(context: Context, intent: Intent) {
		super.onReceive(context, intent)
		if(intent.action == "DAILY_LESSON"){
			context.startActivity(Intent(context, ComposeActivity::class.java).putExtra("page", "dailyLesson"))
		}
	}

	// 每删除一次窗口小部件就调用一次
	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		super.onDeleted(context, appWidgetIds)
	}

	// 当该窗口小部件第一次添加到桌面时调用该方法
	override fun onEnabled(context: Context) {
		super.onEnabled(context)
		remoteView = RemoteViews(context.packageName, R.layout.widget_day_lesson)
	}

	// 当最后一个该窗口小部件删除时调用该方法
	override fun onDisabled(context: Context) {
		super.onDisabled(context)
	}

	// 当小部件从备份恢复时调用该方法
	override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
		super.onRestored(context, oldWidgetIds, newWidgetIds)
	}
}
