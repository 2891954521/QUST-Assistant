package com.qust.helper.next.ui.business.lesson.table

import androidx.compose.runtime.Immutable
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
import kotlinx.datetime.LocalDate

@Immutable
data class LessonTableData(
	val timeTable: TimeTable,
	val startDay: LocalDate,
	val totalWeek: Int,
	val lessons: List<Lesson>,
)
