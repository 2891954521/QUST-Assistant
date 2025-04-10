package com.qust.helper.ui.widget.lesson.lessonTable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.ui.theme.LESSON_BACKGROUND_COLORS
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import java.util.Calendar
import java.util.Date

@Composable
fun LessonTableUI(uiState: LessonTableUIState){
	val density = LocalDensity.current
	val pagerState = rememberPagerState(initialPage = 0, pageCount = { uiState.totalWeek })

	Column {
		Text(
			text = "第 ${uiState.currentWeek + 1} 周",
			modifier = Modifier.fillMaxWidth(),
			style = MaterialTheme.typography.titleLarge,
			textAlign = TextAlign.Center
		)
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize()
		) { page ->
			LessonViewLayout({
				LessonTimeBar(uiState.timeTable)
			}, {
				LessonDate(uiState.startDay, page)
			}, {
				LessonContent(uiState.timeTable.count, uiState.lessonGroupRender)
			})
		}
	}
}

/**
 * 侧面时间栏
 */
@Composable
fun LessonTimeBar(timeTable: TimeTable) {
	Column(modifier = Modifier.fillMaxHeight()) {
		repeat(timeTable.startTime.size){ index ->
			Column(
				modifier = Modifier.weight(1F).padding(horizontal = 4.dp),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(text = (index + 1).toString(), style = MaterialTheme.typography.labelMedium)
				Text(text = timeTable.startTimeStr[index], style = MaterialTheme.typography.bodySmall, color = Color.Gray)
				Text(text = timeTable.endTimeStr[index], style = MaterialTheme.typography.bodySmall, color = Color.Gray)
			}
		}
	}
}

/**
 * 顶部日期栏
 */
@Composable
fun LessonDate(startDay: Date, week: Int) {
	val currentDay = Calendar.getInstance()
	val c = Calendar.getInstance().also { it.time = startDay }
	c[Calendar.WEEK_OF_YEAR] += week
	c[Calendar.DATE] -= (c[Calendar.DAY_OF_WEEK] - 2)

	Box {
		Text(
			text = "${c[Calendar.MONTH] + 1}月",
			modifier = Modifier.align(Alignment.Center),
		)
	}

	Row(modifier = Modifier.fillMaxWidth()) {
		repeat(Strings.ARRAY_WEEK_NAME.size) {
			val color = if(currentDay[Calendar.DATE] == c[Calendar.DATE] && currentDay[Calendar.MONTH] == c[Calendar.MONTH]) {
				LESSON_TEXT_COLORS[0]
			} else Color.Gray

			Column(modifier = Modifier.weight(1F).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
				Text(text = Strings.ARRAY_WEEK_NAME[it], style = MaterialTheme.typography.bodySmall, color = color)
				Text(text = if(c[Calendar.DATE] == 1) "${c[Calendar.MONTH] + 1}月" else "${c[Calendar.DATE]}", style = MaterialTheme.typography.bodySmall, color = color)
			}
			c[Calendar.DATE] += 1
		}
	}
}

/**
 * 课表内容
 */
@Composable
fun LessonContent(count: Int, lessonGroups: List<LessonGroupRenderAble>){
	Layout(content = {
		lessonGroups.forEach {
			LessonGroupItem(it)
		}
	}) { measurables, constraints ->
		val maxWidth = constraints.maxWidth / 7
		val ceilHeight = constraints.maxHeight / count

		val ceilConstraint = Constraints(minWidth = maxWidth, maxWidth = maxWidth, minHeight = 0, maxHeight = constraints.maxHeight)

		val placeables = measurables.mapIndexed { i, measurable ->
			val lesson = lessonGroups[i]
			val height = ((lesson.endOffset - lesson.startOffset) * ceilHeight).toInt()
			measurable.measure(ceilConstraint.copy(minHeight = height, maxHeight = height))
		}

		layout(constraints.maxWidth, constraints.maxHeight) {
			placeables.forEachIndexed { index, placeable ->
				val lesson = lessonGroups[index]
				placeable.place(x = lesson.week * maxWidth, y = (lesson.startOffset * ceilHeight).toInt())
			}
		}
	}
}

@Composable
fun LessonGroupItem(group: LessonGroupRenderAble){
	if(group.lessons.isEmpty()){
		Column {
			Text("")
			Text("")
			Text("")
		}
	}else{
		val lesson = group.lessons[0]
		LessonItem(lesson)
	}

}

@Composable
fun LessonItem(lesson: LessonRenderAble){
	Column(
		modifier = Modifier.padding(1.dp).background(color = LESSON_BACKGROUND_COLORS[lesson.colorIndex], RoundedCornerShape(4.dp)),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		ProvideTextStyle(TextStyle.Default.copy(color = LESSON_TEXT_COLORS[lesson.colorIndex])){
			Text(lesson.name, maxLines = 3)
			Text(lesson.place, maxLines = 2)
			Text(lesson.teacher, maxLines = 1)
		}
	}
}


@Composable
fun LessonViewLayout(timeBar: @Composable () -> Unit, dateBar: @Composable () -> Unit, lessonTable: @Composable () -> Unit) {
	SubcomposeLayout(measurePolicy = { constraints ->
		// 测量出时间栏的宽度
		val timeWidth = subcompose(SlotsEnum.Dependent, timeBar).first().measure(constraints).width

		val tmp = subcompose(SlotsEnum.Date, dateBar)
		val monthBar = tmp[0]
		val dateBar = tmp[1]

		// 带上限制条件测量日期栏
		val dateBarPlace = dateBar.measure(
			constraints.copy(maxWidth = (constraints.maxWidth - timeWidth))
		)

		// 测量出日期栏的高度
		val dateHeight = dateBarPlace.height

		// 带上限制条件测量月份栏
		val monthBarPlace = monthBar.measure(
			Constraints(timeWidth, timeWidth, dateHeight, dateHeight)
		)

		// 带上限制条件再次测量时间栏
		val timeBarPlace = subcompose(SlotsEnum.Time, timeBar).first().measure(
			Constraints(timeWidth, timeWidth, (constraints.maxHeight - dateHeight), (constraints.maxHeight - dateHeight))
		)

		// 带上限制条件测量课表
		val lessonTablePlace = subcompose(SlotsEnum.Main, lessonTable).first().measure(
			constraints.copy(maxWidth = (constraints.maxWidth - timeWidth), maxHeight = (constraints.maxHeight - dateHeight))
		)

		layout(constraints.maxWidth, constraints.maxHeight) {
			monthBarPlace.place(0, 0)
			dateBarPlace.place(timeWidth, 0)
			timeBarPlace.place(0, dateHeight)
			lessonTablePlace.place(timeWidth, dateHeight)
		}
	})
}


internal enum class SlotsEnum { Dependent, Time, Date, Main }