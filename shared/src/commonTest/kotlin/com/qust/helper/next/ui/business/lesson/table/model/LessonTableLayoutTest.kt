package com.qust.helper.next.ui.business.lesson.table.model

import com.qust.helper.next.entity.lesson.LessonTableData
import com.qust.helper.next.entity.lesson.TimeTablePresets
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.lesson.table.axis.SlotTimeAxis
import com.qust.helper.next.ui.business.lesson.table.axis.hm
import com.qust.helper.next.ui.business.lesson.table.axis.lesson
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LessonTableLayoutTest {

	@Test
	fun startDayNormalizedToMonday() {
		val layout = computeLessonTableLayout(
			data = LessonTableData(
				timeTable = TimeTablePresets.WINTER_NEW.timeTable,
				startDay = LocalDate(2026, 3, 4),
				totalWeek = 4,
				lessons = emptyList(),
			),
			settings = LessonTableRepository.LessonTableSetting(),
			axisFactory = SlotTimeAxis.Factory,
		)
		assertEquals(LocalDate(2026, 3, 2), layout.startDay)
	}

	@Test
	fun dayCountFollowsShowWeekend() {
		val data = LessonTableData(
			timeTable = TimeTablePresets.WINTER_NEW.timeTable,
			startDay = LocalDate(2026, 3, 2),
			totalWeek = 4,
			lessons = emptyList(),
		)
		val withWeekend = computeLessonTableLayout(data, LessonTableRepository.LessonTableSetting(showWeekend = true), SlotTimeAxis.Factory)
		val withoutWeekend = computeLessonTableLayout(data, LessonTableRepository.LessonTableSetting(showWeekend = false), SlotTimeAxis.Factory)
		assertEquals(7, withWeekend.dayCount)
		assertEquals(5, withoutWeekend.dayCount)
	}

	@Test
	fun outOfRangeWeekIsFiltered() {
		val layout = computeLessonTableLayout(
			data = LessonTableData(
				timeTable = TimeTablePresets.WINTER_NEW.timeTable,
				startDay = LocalDate(2026, 3, 2),
				totalWeek = 4,
				lessons = listOf(
					lesson(name = "合法", start = hm(8, 0), end = hm(8, 50), week = 1),
					lesson(name = "越界", start = hm(8, 0), end = hm(8, 50), week = 8),
				),
			),
			settings = LessonTableRepository.LessonTableSetting(),
			axisFactory = SlotTimeAxis.Factory,
		)
		assertEquals(1, layout.groups.size)
		assertEquals("合法", layout.groups.single().entries.single().lesson.name)
	}

	@Test
	fun totalWeekBelowOneBecomesOne() {
		val layout = computeLessonTableLayout(
			data = LessonTableData(
				timeTable = TimeTablePresets.WINTER_NEW.timeTable,
				startDay = LocalDate(2026, 3, 2),
				totalWeek = 0,
				lessons = emptyList(),
			),
			settings = LessonTableRepository.LessonTableSetting(),
			axisFactory = SlotTimeAxis.Factory,
		)
		assertEquals(1, layout.totalWeek)
	}

	@Test
	fun weekendLessonsAreDroppedWhenHidden() {
		val layout = computeLessonTableLayout(
			data = LessonTableData(
				timeTable = TimeTablePresets.WINTER_NEW.timeTable,
				startDay = LocalDate(2026, 3, 2),
				totalWeek = 4,
				lessons = listOf(
					lesson(name = "周五", start = hm(8, 0), end = hm(8, 50), week = 4),
					lesson(name = "周六", start = hm(7, 0), end = hm(7, 40), week = 5),
				),
			),
			settings = LessonTableRepository.LessonTableSetting(showWeekend = false),
			axisFactory = SlotTimeAxis.Factory,
		)
		assertEquals(5, layout.dayCount)
		assertEquals(listOf("周五"), layout.groups.flatMap { it.entries }.map { it.lesson.name })
		assertTrue(layout.axis.segments.none { it.kind == com.qust.helper.next.ui.business.lesson.table.axis.TimeSegment.Kind.EXTRA })
	}
}
