package com.qust.helper.next.ui.business.lesson.table.axis

import androidx.compose.runtime.Immutable
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
import kotlin.math.roundToInt

interface LessonTimeAxis {
	val segments: List<TimeSegment>
	val totalWeight: Float
	val startMinute: Int
	val endMinute: Int
	fun offsetOf(minute: Int): Float
}

@Immutable
data class TimeSegment(
	val startMinute: Int,
	val endMinute: Int,
	val weight: Float,
	val kind: Kind,
	val slotIndex: Int,
) {
	enum class Kind { SLOT, GAP, EXTRA }
}

fun interface LessonTimeAxisFactory {
	fun create(timeTable: TimeTable, lessons: List<Lesson>): LessonTimeAxis
}

fun LessonTimeAxis.toPx(minute: Int, heightPx: Int): Int {
	if(totalWeight <= 0f || heightPx <= 0) return 0
	return (offsetOf(minute) / totalWeight * heightPx).roundToInt()
}

fun LessonTimeAxis.offsetToPx(offset: Float, heightPx: Int): Int {
	if(totalWeight <= 0f || heightPx <= 0) return 0
	return (offset / totalWeight * heightPx).roundToInt()
}

internal fun formatMinuteOfDay(minute: Int): String {
	val clamped = minute.coerceAtLeast(0)
	val hour = clamped / 60
	val min = clamped % 60
	return "${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}"
}

internal fun floorTo10(minute: Int): Int = minute / 10 * 10

internal fun ceilTo10(minute: Int): Int = (minute + 9) / 10 * 10
