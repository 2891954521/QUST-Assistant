package com.qust.helper.next.ui.business.lesson.table.model

import com.qust.helper.next.ui.business.lesson.table.LessonTableSettings
import com.qust.helper.next.ui.business.lesson.table.axis.hm
import com.qust.helper.next.ui.business.lesson.table.axis.lesson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LessonGroupResolveTest {

	@Test
	fun currentWeekUsesAllCurrentLessons() {
		val group = LessonGroup.create(
			0,
			listOf(
				entry("本周B", hm(8, 10), hm(8, 40), weeks = 1L),
				entry("本周A", hm(8, 0), hm(8, 50), weeks = 1L),
				entry("未来", hm(8, 0), hm(8, 50), weeks = 1L shl 2),
			),
		)
		val resolved = group.resolve(0, LessonTableSettings())
		assertEquals(listOf("本周A", "本周B"), resolved.map { it.entry.lesson.name })
		assertTrue(resolved.all { it.status == LessonStatus.CURRENT })
	}

	@Test
	fun emptyWhenNoCurrentAndSettingsOff() {
		val group = LessonGroup.create(
			0,
			listOf(
				entry("未来", hm(8, 0), hm(8, 50), weeks = 1L shl 2),
				entry("过去", hm(8, 0), hm(8, 50), weeks = 1L),
			),
		)
		assertTrue(group.resolve(1, LessonTableSettings()).isEmpty())
	}

	@Test
	fun upcomingOnlyWhenShowNonCurrentWeek() {
		val group = LessonGroup.create(
			0,
			listOf(
				entry("更远未来", hm(8, 0), hm(8, 50), weeks = 1L shl 5),
				entry("最近未来", hm(8, 0), hm(8, 50), weeks = 1L shl 2),
				entry("过去", hm(8, 0), hm(8, 50), weeks = 1L),
			),
		)
		val resolved = group.resolve(1, LessonTableSettings(showNonCurrentWeek = true))
		assertEquals(listOf("最近未来", "更远未来"), resolved.map { it.entry.lesson.name })
		assertTrue(resolved.all { it.status == LessonStatus.UPCOMING })
	}

	@Test
	fun showFinishedWithoutNonCurrentWeekIsEmpty() {
		val group = LessonGroup.create(
			0,
			listOf(
				entry("未来", hm(8, 0), hm(8, 50), weeks = 1L shl 2),
				entry("过去", hm(8, 0), hm(8, 50), weeks = 1L),
			),
		)
		assertTrue(group.resolve(1, LessonTableSettings(showFinished = true)).isEmpty())
	}

	@Test
	fun upcomingThenFinishedWithoutDuplicates() {
		val group = LessonGroup.create(
			0,
			listOf(
				entry("更远未来", hm(8, 0), hm(8, 50), weeks = 1L shl 6),
				entry("最近未来", hm(8, 0), hm(8, 50), weeks = 1L shl 4),
				entry("两边都有", hm(8, 0), hm(8, 50), weeks = (1L shl 1) or (1L shl 5)),
				entry("最近过去", hm(8, 0), hm(8, 50), weeks = 1L shl 2),
				entry("更早过去", hm(8, 0), hm(8, 50), weeks = 1L),
			),
		)
		val resolved = group.resolve(
			week = 3,
			settings = LessonTableSettings(showNonCurrentWeek = true, showFinished = true),
		)
		assertEquals(
			listOf("最近未来", "两边都有", "更远未来", "最近过去", "更早过去"),
			resolved.map { it.entry.lesson.name },
		)
		assertEquals(
			listOf(
				LessonStatus.UPCOMING,
				LessonStatus.UPCOMING,
				LessonStatus.UPCOMING,
				LessonStatus.FINISHED,
				LessonStatus.FINISHED,
			),
			resolved.map { it.status },
		)
	}

	private fun entry(name: String, start: Int, end: Int, weeks: Long): LessonEntry {
		val lesson = lesson(name = name, start = start, end = end, weeks = weeks)
		return LessonEntry(lesson, lesson.key(), 0f, 1f)
	}
}
