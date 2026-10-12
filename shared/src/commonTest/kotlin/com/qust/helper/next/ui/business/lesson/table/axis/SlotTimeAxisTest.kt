package com.qust.helper.next.ui.business.lesson.table.axis

import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlotTimeAxisTest {

	private val timeTable = TimeTable.DEFAULT
	private val avgSlot = 50f

	@Test
	fun defaultTimetableSegmentsAndWeights() {
		val axis = SlotTimeAxis.create(timeTable, emptyList())
		val slots = axis.segments.filter { it.kind == TimeSegment.Kind.SLOT }
		val gaps = axis.segments.filter { it.kind == TimeSegment.Kind.GAP }
		assertEquals(10, slots.size)
		assertEquals(9, gaps.size)
		assertTrue(axis.segments.none { it.kind == TimeSegment.Kind.EXTRA })
		slots.forEach { assertEquals(1f, it.weight) }
		assertEquals(0f, gapAt(axis, 8, 50, 9, 0).weight)
		assertFloatEquals(0.3f, gapAt(axis, 12, 0, 13, 30).weight)
	}

	@Test
	fun shortGapHasZeroWeight() {
		val axis = SlotTimeAxis.create(timeTable, emptyList())
		assertEquals(0f, gapAt(axis, 8, 50, 9, 0).weight)
		assertEquals(0f, gapAt(axis, 11, 0, 11, 10).weight)
	}

	@Test
	fun lunchGapHasLongWeight() {
		val axis = SlotTimeAxis.create(timeTable, emptyList())
		assertFloatEquals(0.3f, gapAt(axis, 12, 0, 13, 30).weight)
	}

	@Test
	fun lunchGapWeightRisesWhenLessonIntersects() {
		val lesson = lesson(name = "午休课", start = hm(12, 10), end = hm(13, 0))
		val axis = SlotTimeAxis.create(timeTable, listOf(lesson))
		assertFloatEquals(90f / avgSlot, gapAt(axis, 12, 0, 13, 30).weight)
	}

	@Test
	fun shortGapWeightRisesWhenLessonFullyInside() {
		val lesson = lesson(name = "课间", start = hm(8, 52), end = hm(8, 58))
		val axis = SlotTimeAxis.create(timeTable, listOf(lesson))
		assertTrue(gapAt(axis, 8, 50, 9, 0).weight > 0f)
	}

	@Test
	fun extraSegmentsAlignToTenMinutes() {
		val early = lesson(name = "早课", start = hm(7, 5), end = hm(7, 40))
		val late = lesson(name = "晚课", start = hm(20, 30), end = hm(21, 31))
		val axis = SlotTimeAxis.create(timeTable, listOf(early, late))
		val head = axis.segments.first()
		val tail = axis.segments.last()
		assertEquals(TimeSegment.Kind.EXTRA, head.kind)
		assertEquals(floorTo10(hm(7, 5)), head.startMinute)
		assertEquals(timeTable.startMinute.first(), head.endMinute)
		assertTrue(head.weight >= 0.5f)
		assertEquals(TimeSegment.Kind.EXTRA, tail.kind)
		assertEquals(timeTable.endMinute.last(), tail.startMinute)
		assertEquals(ceilTo10(hm(21, 31)), tail.endMinute)
	}

	@Test
	fun offsetOfClampsAndIsLinearAndMonotonic() {
		val axis = SlotTimeAxis.create(timeTable, emptyList())
		assertEquals(0f, axis.offsetOf(0))
		assertEquals(axis.totalWeight, axis.offsetOf(24 * 60))
		val firstSlot = axis.segments.first { it.kind == TimeSegment.Kind.SLOT }
		val mid = (firstSlot.startMinute + firstSlot.endMinute) / 2
		assertFloatEquals(0.5f, axis.offsetOf(mid))
		var previous = axis.offsetOf(axis.startMinute)
		for(minute in axis.startMinute..axis.endMinute) {
			val current = axis.offsetOf(minute)
			assertTrue(current + 1e-4f >= previous, "offsetOf is not monotonic at $minute")
			previous = current
		}
	}

	private fun gapAt(axis: LessonTimeAxis, startH: Int, startM: Int, endH: Int, endM: Int): TimeSegment {
		val start = hm(startH, startM)
		val end = hm(endH, endM)
		return axis.segments.first { it.kind == TimeSegment.Kind.GAP && it.startMinute == start && it.endMinute == end }
	}
}

internal fun hm(hour: Int, minute: Int): Int = hour * 60 + minute

internal fun lesson(
	name: String,
	start: Int,
	end: Int,
	week: Int = 0,
	weeks: Long = 1L,
	id: Long = 0L,
	place: String = "",
	teacher: String = "",
	colorLabel: Int = 0,
): Lesson = Lesson(
	id = id,
	name = name,
	week = week,
	startMinute = start,
	endMinute = end,
	weeks = weeks,
	place = place,
	teacher = teacher,
	colorLabel = colorLabel,
)

internal fun assertFloatEquals(expected: Float, actual: Float, epsilon: Float = 1e-4f) {
	assertTrue(abs(expected - actual) <= epsilon, "expected $expected but was $actual")
}
