package com.qust.helper.next.ui.business.lesson.table.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.theme.Theme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus

@Composable
fun MonthCorner(
	startDay: LocalDate,
	week: Int,
	modifier: Modifier = Modifier,
) {
	val date = startDay.plus(week, DateTimeUnit.WEEK)
	Box(modifier, contentAlignment = Alignment.Center) {
		Text(
			text = "${date.month.number}月",
			style = Theme.textStyles.body,
		)
	}
}

@Composable
fun LessonDateBar(
	startDay: LocalDate,
	week: Int,
	dayCount: Int,
	today: LocalDate,
	colors: LessonTableColors,
	modifier: Modifier = Modifier,
) {
	var date = startDay.plus(week, DateTimeUnit.WEEK)
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		repeat(dayCount) { index ->
			val color = if(date == today) colors.todayText else colors.timeBarSecondaryText
			Column(
				modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = Strings.ARRAY_WEEK_NAME[index],
					style = Theme.textStyles.body,
					color = color,
				)
				Text(
					text = if(date.day == 1) "${date.month.number}月" else "${date.day}",
					style = Theme.textStyles.body,
					color = color,
				)
			}
			date = date.plus(1, DateTimeUnit.DAY)
		}
	}
}
