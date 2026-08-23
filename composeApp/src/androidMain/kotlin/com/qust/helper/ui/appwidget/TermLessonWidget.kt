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
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.model.SettingModel
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.activity.MainActivity
import com.qust.helper.ui.theme.LESSON_BACKGROUND_COLORS
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.utils.DateUtils
import kotlinx.datetime.LocalDate

private val DAY_SHORT_NAMES = listOf("一", "二", "三", "四", "五", "六", "日")

/**
 * 学期课表桌面小组件，展示本周课程
 */
class TermLessonWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val lessons = LessonTableRepository.getAllLesson()
		val today = DateUtils.today()
		val currentWeek = if(SettingModel.startDay != LocalDate(1, 1, 1)) {
			DateUtils.calcWeekOffset(SettingModel.startDay, today).coerceAtLeast(0)
		} else 0
		val totalWeek = SettingModel.totalWeek

		val weekLessons = lessons
			.filter { (it.weeks and (1L shl currentWeek)) > 0 }
			.groupBy { it.week }
			.mapValues { it.value.sortedBy { lesson -> lesson.startMinute } }

		val openAction = actionStartActivity(
			Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		)

		provideContent {
			GlanceTheme {
				TermLessonView(currentWeek, totalWeek, weekLessons, openAction)
			}
		}
	}
}

@Composable
fun TermLessonView(currentWeek: Int, totalWeek: Int, weekLessons: Map<Int, List<Lesson>>, openAction: Action) {
	Column(
		modifier = GlanceModifier.fillMaxSize().padding(8.dp).clickable(openAction),
		verticalAlignment = Alignment.Top,
		horizontalAlignment = Alignment.Start
	) {
		Text(
			text = "第 ${currentWeek + 1} / $totalWeek 周",
			modifier = GlanceModifier.fillMaxWidth(),
			style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = GlanceTheme.colors.onSurface)
		)
		Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp)) {
			for(day in 0 until 7) {
				val dayLessons = weekLessons[day].orEmpty()
				Column(modifier = GlanceModifier.defaultWeight().padding(horizontal = 1.dp)) {
					Text(
						text = DAY_SHORT_NAMES[day],
						modifier = GlanceModifier.fillMaxWidth(),
						style = TextStyle(fontSize = 10.sp, textAlign = TextAlign.Center, color = GlanceTheme.colors.onSurfaceVariant)
					)
					dayLessons.take(3).forEach { lesson ->
						Text(
							text = lesson.name,
							modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp).background(LESSON_BACKGROUND_COLORS[lesson.colorLabel]),
							style = TextStyle(fontSize = 8.sp, textAlign = TextAlign.Center, color = ColorProvider(LESSON_TEXT_COLORS[lesson.colorLabel])),
							maxLines = 1
						)
					}
				}
			}
		}
	}
}

class TermLessonWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = TermLessonWidget()
}
