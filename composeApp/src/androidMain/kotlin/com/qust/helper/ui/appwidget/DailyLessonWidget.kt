package com.qust.helper.ui.appwidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.activity.MainActivity
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.utils.DateUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

private val WEEK_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/**
 * 当日课表桌面小组件
 */
class DailyLessonWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val timeTable = LessonTableRepository.resolveTimeTable()
		val lessons = LessonTableRepository.getAllLesson()
		val today = DateUtils.today()
		val weekOfDay = today.dayOfWeek.isoDayNumber - 1
		val currentWeek = if(SettingModel.startDay != LocalDate(1, 1, 1)) {
			DateUtils.calcWeekOffset(SettingModel.startDay, today).coerceAtLeast(0)
		} else 0

		val dayLessons = lessons.filter { it.week == weekOfDay && (it.weeks and (1L shl currentWeek)) > 0 }
			.sortedBy { it.startMinute }
			.take(8)

		val openAction = actionStartActivity(
			Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		)

		provideContent {
			GlanceTheme {
				DailyLessonView(dayLessons, weekOfDay, currentWeek, timeTable, openAction)
			}
		}
	}
}

@Composable
fun DailyLessonView(lessons: List<Lesson>, weekOfDay: Int, currentWeek: Int, timeTable: TimeTable, openAction: Action) {
	Column(
		modifier = GlanceModifier.fillMaxSize().padding(12.dp).clickable(openAction),
		verticalAlignment = Alignment.Top,
		horizontalAlignment = Alignment.Start
	) {
		Text(
			text = "${WEEK_NAMES[weekOfDay]} · 第 ${currentWeek + 1} 周",
			style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
		)
		if(lessons.isEmpty()) {
			Text(
				text = "暂无课程，点击进入应用查询课表",
				modifier = GlanceModifier.padding(top = 12.dp),
				style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant)
			)
		} else {
			lessons.forEach { lesson ->
				Row(
					modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Column(modifier = GlanceModifier.defaultWeight()) {
						Text(
							text = lesson.name,
							style = TextStyle(fontSize = 13.sp, color = ColorProvider(LESSON_TEXT_COLORS[lesson.colorLabel])),
							maxLines = 1
						)
						val info = buildString {
							if(lesson.place.isNotEmpty()) append(lesson.place)
							if(lesson.teacher.isNotEmpty()) {
								if(isNotEmpty()) append(" | ")
								append(lesson.teacher)
							}
						}
						if(info.isNotEmpty()) {
							Text(
								text = info,
								style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant),
								maxLines = 1
							)
						}
					}
					Text(
						text = lessonTimeText(lesson, timeTable),
						style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
					)
				}
			}
		}
	}
}

private fun lessonTimeText(lesson: Lesson, timeTable: TimeTable): String {
	val start = timeTable.startMinute.indexOf(lesson.startMinute)
	val end = timeTable.endMinute.indexOf(lesson.endMinute)
	return if(start >= 0 && end >= 0) {
		"${timeTable.startTimeStr[start]}-${timeTable.endTimeStr[end]}"
	} else {
		""
	}
}

class DailyLessonWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = DailyLessonWidget()
}
