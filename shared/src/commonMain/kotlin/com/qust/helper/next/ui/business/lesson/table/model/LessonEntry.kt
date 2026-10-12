package com.qust.helper.next.ui.business.lesson.table.model

import androidx.compose.runtime.Immutable
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.ui.business.lesson.table.axis.LessonTimeAxis
import com.qust.helper.next.ui.business.lesson.table.axis.SlotTimeAxis

@Immutable
data class LessonEntry(
	val lesson: Lesson,
	val key: LessonKey,
	val startOffset: Float,
	val endOffset: Float,
) {
	fun hasWeek(week: Int): Boolean {
		if(week < 0 || week >= 64) return false
		return (lesson.weeks shr week) and 1L == 1L
	}

	fun firstWeekAfter(week: Int): Int? {
		val from = week + 1
		if(from >= 64) return null
		for(w in from..63) {
			if((lesson.weeks shr w) and 1L == 1L) return w
		}
		return null
	}

	fun lastWeekBefore(week: Int): Int? {
		val until = week.coerceAtMost(64) - 1
		if(until < 0) return null
		for(w in until downTo 0) {
			if((lesson.weeks shr w) and 1L == 1L) return w
		}
		return null
	}

	companion object {
		fun from(
			lesson: Lesson,
			axis: LessonTimeAxis,
			minCellWeight: Float = SlotTimeAxis.MIN_CELL_WEIGHT,
		): LessonEntry {
			val startOffset = axis.offsetOf(lesson.startMinute)
			var endOffset = axis.offsetOf(lesson.endMinute)
			if(endOffset < startOffset + minCellWeight) {
				endOffset = startOffset + minCellWeight
			}
			return LessonEntry(lesson, lesson.key(), startOffset, endOffset)
		}
	}
}
