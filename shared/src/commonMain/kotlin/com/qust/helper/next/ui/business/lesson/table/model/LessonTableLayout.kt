package com.qust.helper.next.ui.business.lesson.table.model

import androidx.compose.runtime.Immutable
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.lesson.table.LessonTableData
import com.qust.helper.next.ui.business.lesson.table.axis.LessonTimeAxis
import com.qust.helper.next.ui.business.lesson.table.axis.LessonTimeAxisFactory
import com.qust.helper.next.ui.business.lesson.table.axis.SlotTimeAxis
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Immutable
data class LessonTableLayout(
	val axis: LessonTimeAxis,
	val groups: List<LessonGroup>,
	val totalWeek: Int,
	val startDay: LocalDate,
	val dayCount: Int,
)

fun computeLessonTableLayout(
	data: LessonTableData,
	settings: LessonTableRepository.LessonTableSetting,
	axisFactory: LessonTimeAxisFactory,
	grouper: LessonGrouper = OverlapLessonGrouper,
): LessonTableLayout {
	val totalWeek = data.totalWeek.coerceAtLeast(1)
	val startDay = data.startDay.plus(-data.startDay.dayOfWeek.ordinal, DateTimeUnit.DAY)
	val dayCount = if(settings.showWeekend) 7 else 5

	val validLessons = data.lessons.filter { lesson ->
		when {
			lesson.week !in 0..6 -> {
				Logger.w("Ignore lesson '${lesson.name}' with invalid week ${lesson.week}")
				false
			}
			lesson.week >= dayCount -> false
			else -> true
		}
	}

	val axis = axisFactory.create(data.timeTable, validLessons)
	val entriesByDay = Array(dayCount) { ArrayList<LessonEntry>() }
	for(lesson in validLessons) {
		entriesByDay[lesson.week] += LessonEntry.from(lesson, axis, SlotTimeAxis.MIN_CELL_WEIGHT)
	}

	val groups = entriesByDay.flatMap { grouper.group(it) }
	return LessonTableLayout(
		axis = axis,
		groups = groups,
		totalWeek = totalWeek,
		startDay = startDay,
		dayCount = dayCount,
	)
}
