package com.qust.helper.ui.page.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.ViewList
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.viewmodel.lesson.DailyLessonViewModel

object DailyLessonPage: BasePage<DailyLessonViewModel>("当日课表", Drawables.ViewList) {

	@Composable
	override fun getViewModel() = viewModel<DailyLessonViewModel>()

	@Composable
	override fun Content(viewModel: DailyLessonViewModel) {
		LaunchedEffect(Unit) {
			viewModel.refresh()
		}

		Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
			val dateText = viewModel.dayOfWeekText
			val weekText = "第 ${viewModel.currentWeek + 1} 周"
			Text(
				text = "$dateText · $weekText",
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
			)

			if(viewModel.lessons.isEmpty()) {
				Text(
					text = "今天没有课程",
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(top = 32.dp)
				)
			}

			LazyColumn {
				LessonSection(viewModel, title = "上午课程", start = 0, end = 720, period = 0..3)
				LessonSection(viewModel, title = "下午课程", start = 720, end = 1080, period = 4..7)
				LessonSection(viewModel, title = "晚上课程", start = 1080, end = Int.MAX_VALUE, period = 8..9)
			}
		}
	}

	private fun LazyListScope.LessonSection(viewModel: DailyLessonViewModel, title: String, start: Int, end: Int, period: IntRange) {
		item {
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				color = colorSecondaryText,
				modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
			)
		}

		val lessons = viewModel.lessons.filter { it.startMinute in start until end }
		if(lessons.isEmpty()) {
			item { FreeCard(viewModel, period) }
		} else {
			items(lessons, key = { it.id }) { lesson ->
				LessonCard(lesson)
			}
		}
	}

	@Composable
	fun LessonCard(lesson: Lesson) {
		val color = LESSON_TEXT_COLORS[lesson.colorLabel % LESSON_TEXT_COLORS.size]
		Card(
			modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		) {
			Row(modifier = Modifier.padding(12.dp)) {
				Column(modifier = Modifier.weight(1F)) {
					Text(
						text = lesson.name.ifEmpty { "未命名课程" },
						style = MaterialTheme.typography.titleMedium,
						color = color
					)
					if(lesson.place.isNotEmpty()) {
						Text(text = "教室: ${lesson.place}", style = MaterialTheme.typography.bodyMedium)
					}
					if(lesson.teacher.isNotEmpty()) {
						Text(text = "教师: ${lesson.teacher}", style = MaterialTheme.typography.bodyMedium)
					}
				}
				Text(
					text = "${lesson.startMinute / 60}:${(lesson.startMinute % 60).toString().padStart(2, '0')} - ${lesson.endMinute / 60}:${(lesson.endMinute % 60).toString().padStart(2, '0')}",
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.padding(start = 8.dp)
				)
			}
		}
	}

	@Composable
	private fun FreeCard(viewModel: DailyLessonViewModel, period: IntRange) {
		val tt = viewModel.timeTable
		val timeText = if(period.last < tt.startMinute.size && period.last < tt.endMinute.size)
			"${formatMinute(tt.startMinute[period.first])} - ${formatMinute(tt.endMinute[period.last])}"
		else ""

		Card(
			modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		) {
			Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = "空闲",
					style = MaterialTheme.typography.titleMedium,
					color = colorSecondaryText,
					modifier = Modifier.weight(1F)
				)
				Text(
					text = timeText,
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText
				)
			}
		}
	}

	private fun formatMinute(minute: Int): String =
		"${minute / 60}:${(minute % 60).toString().padStart(2, '0')}"
}
