package com.qust.helper.next.ui.business.lesson.table.model

import com.qust.helper.next.ui.business.lesson.table.axis.hm
import com.qust.helper.next.ui.business.lesson.table.axis.lesson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OverlapLessonGrouperTest {

	@Test
	fun disjointLessonsStaySeparate() {
		val groups = OverlapLessonGrouper.group(
			listOf(
				entry("A", hm(8, 0), hm(8, 50)),
				entry("B", hm(10, 10), hm(11, 0)),
			)
		)
		assertEquals(2, groups.size)
	}

	@Test
	fun adjacentLessonsDoNotMerge() {
		val groups = OverlapLessonGrouper.group(
			listOf(
				entry("A", hm(8, 0), hm(9, 0)),
				entry("B", hm(9, 0), hm(10, 0)),
			)
		)
		assertEquals(2, groups.size)
	}

	@Test
	fun connectedOverlapFormsOneGroup() {
		val groups = OverlapLessonGrouper.group(
			listOf(
				entry("A", hm(8, 0), hm(9, 0)),
				entry("B", hm(8, 30), hm(9, 30)),
				entry("C", hm(9, 0), hm(10, 0)),
			)
		)
		assertEquals(1, groups.size)
		assertEquals(listOf("A", "B", "C"), groups[0].entries.map { it.lesson.name })
	}

	@Test
	fun differentWeeksAtSameTimeMerge() {
		val groups = OverlapLessonGrouper.group(
			listOf(
				entry("本周", hm(8, 0), hm(8, 50), weeks = 1L),
				entry("下周", hm(8, 0), hm(8, 50), weeks = 1L shl 1),
			)
		)
		assertEquals(1, groups.size)
		assertEquals(2, groups[0].entries.size)
	}

	@Test
	fun sortIsStableForEqualStartAndEnd() {
		val first = entry("先", hm(8, 0), hm(8, 50), id = 1)
		val second = entry("后", hm(8, 0), hm(8, 50), id = 2)
		val groups = OverlapLessonGrouper.group(listOf(first, second))
		assertEquals(listOf("先", "后"), groups.single().entries.map { it.lesson.name })
	}

	@Test
	fun emptyInput() {
		assertTrue(OverlapLessonGrouper.group(emptyList()).isEmpty())
	}

	private fun entry(
		name: String,
		start: Int,
		end: Int,
		weeks: Long = 1L,
		id: Long = 0L,
	): LessonEntry {
		val lesson = lesson(name = name, start = start, end = end, weeks = weeks, id = id)
		return LessonEntry(lesson, lesson.key(), 0f, 1f)
	}
}
