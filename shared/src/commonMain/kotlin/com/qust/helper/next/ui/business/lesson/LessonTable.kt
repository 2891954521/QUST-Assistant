package com.qust.helper.next.ui.business.lesson

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.LessonTableData
import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.business.lesson.table.axis.LessonTimeAxisFactory
import com.qust.helper.next.ui.business.lesson.table.axis.SlotTimeAxis
import com.qust.helper.next.ui.business.lesson.table.model.computeLessonTableLayout
import com.qust.helper.next.ui.business.lesson.table.state.LessonTableState
import com.qust.helper.next.ui.business.lesson.table.state.rememberLessonTableState
import com.qust.helper.next.ui.business.lesson.table.ui.LessonTableFrame
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.Colors
import com.qust.helper.next.utils.DateUtils
import kotlinx.datetime.LocalDate

object LessonTableDefaults {

	val DateBarHeight = 44.dp
	val TimeBarWidth = 64.dp

	val SlotTimeAxisConfig = SlotTimeAxis.Config()

	@Composable
	fun colors(): LessonTableColors {
		val theme = Theme.color
		return LessonTableColors(
			lessonBackground = Colors.LESSON_BACKGROUND_COLORS.toList(),
			lessonText = Colors.LESSON_TEXT_COLORS.toList(),
			upcomingBackground = Colors.LESSON_BACKGROUND_COLOR_SECOND,
			upcomingText = Colors.LESSON_TEXT_COLOR_SECOND,
			finishedBackground = Colors.LESSON_BACKGROUND_COLOR_FINISHED,
			finishedText = Colors.LESSON_TEXT_COLOR_FINISHED,
			todayText = Colors.LESSON_TEXT_COLORS[0],
			timeBarText = theme.textPrimary,
			timeBarSecondaryText = theme.textSecondary,
			gapLine = theme.outlineVariant,
		)
	}

	@Composable
	fun WeekTitle(week: Int) {
		Text(
			text = "第 ${week + 1} 周",
			modifier = Modifier.fillMaxWidth(),
			style = Theme.textStyles.subtitle,
			textAlign = TextAlign.Center,
		)
	}
}

@Composable
fun LessonTable(
	data: LessonTableData,
	settings: LessonTableRepository.LessonTableSetting,
	modifier: Modifier = Modifier,
	state: LessonTableState = rememberLessonTableState(data),
	colors: LessonTableColors = LessonTableDefaults.colors(),
	axisFactory: LessonTimeAxisFactory = SlotTimeAxis.Factory,
	title: @Composable (week: Int) -> Unit = { LessonTableDefaults.WeekTitle(it) },
	onCellClick: (LessonCellInfo) -> Unit = {},
	onCellLongClick: (LessonCellInfo, anchor: IntRect) -> Unit = { _, _ -> },
) {
	val layout = remember(data, settings.showWeekend, axisFactory) {
		computeLessonTableLayout(data, settings, axisFactory)
	}
	val today = remember { DateUtils.currentDate() }
	LessonTableFrame(
		layout = layout,
		settings = settings,
		state = state,
		colors = colors,
		today = today,
		title = title,
		onCellClick = onCellClick,
		onCellLongClick = onCellLongClick,
		modifier = modifier.fillMaxSize(),
	)
}

@Preview(widthDp = 400, heightDp = 720)
@Composable
private fun LessonTablePreviewDefault() = AppPreview {
	LessonTable(
		data = PreviewLessonTableData,
		settings = LessonTableRepository.LessonTableSetting()
	)
}

@Preview(widthDp = 400, heightDp = 720)
@Composable
private fun LessonTablePreviewNonCurrentWeek() = AppPreview {
	LessonTable(
		data = PreviewLessonTableData,
		settings = LessonTableRepository.LessonTableSetting(showNonCurrentWeek = true),
	)
}

@Preview(widthDp = 400, heightDp = 720)
@Composable
private fun LessonTablePreviewFinished() = AppPreview {
	LessonTable(
		data = PreviewLessonTableData,
		settings = LessonTableRepository.LessonTableSetting(showNonCurrentWeek = true, showFinished = true),
	)
}

private val PreviewLessonTableData = LessonTableData(
	timeTable = TimeTable.DEFAULT,
	startDay = LocalDate(2026, 3, 2),
	totalWeek = 4,
	lessons = listOf(
		Lesson(id = 1, name = "早课", week = 0, startMinute = 7 * 60, endMinute = 7 * 60 + 40, weeks = 1L, place = "A101", teacher = "张三", colorLabel = 0),
		Lesson(id = 2, name = "午休课", week = 1, startMinute = 12 * 60 + 10, endMinute = 13 * 60, weeks = 1L, place = "B201", teacher = "李四", colorLabel = 1),
		Lesson(id = 3, name = "晚课", week = 2, startMinute = 20 * 60 + 30, endMinute = 21 * 60 + 30, weeks = 1L, place = "C301", teacher = "王五", colorLabel = 2),
		Lesson(id = 4, name = "高数", week = 3, startMinute = 8 * 60, endMinute = 8 * 60 + 50, weeks = 1L, place = "D101", teacher = "赵六", colorLabel = 3),
		Lesson(id = 5, name = "线代", week = 3, startMinute = 8 * 60 + 20, endMinute = 9 * 60 + 20, weeks = 1L, place = "D102", teacher = "钱七", colorLabel = 4),
		Lesson(id = 6, name = "概率", week = 3, startMinute = 8 * 60 + 40, endMinute = 9 * 60 + 50, weeks = 1L, place = "D103", teacher = "孙八", colorLabel = 5),
		Lesson(id = 7, name = "未来课", week = 4, startMinute = 10 * 60 + 10, endMinute = 11 * 60, weeks = 1L shl 2, place = "E101", teacher = "周九", colorLabel = 6),
		Lesson(id = 8, name = "已结束课", week = 4, startMinute = 14 * 60 + 30, endMinute = 15 * 60 + 20, weeks = 1L, place = "E102", teacher = "吴十", colorLabel = 7),
	),
)
