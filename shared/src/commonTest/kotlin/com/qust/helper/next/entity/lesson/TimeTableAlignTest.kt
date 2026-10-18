package com.qust.helper.next.entity.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimeTableAlignTest {

	private val from = table(8_00 to 8_50, 9_00 to 9_50, 10_10 to 11_00, 11_10 to 12_00)
	private val to = table(8_30 to 9_20, 9_30 to 10_20, 10_30 to 11_20, 11_30 to 12_20)

	@Test
	fun singlePeriodSnapsToNewPeriod() {
		// from 第 3 节 10:10-11:00 的课 → to 第 3 节 10:30-11:20
		assertEquals(10 * 60 + 30 to 11 * 60 + 20, from.mapLessonMinutes(10 * 60 + 10, 11 * 60, to))
	}

	@Test
	fun spannedPeriodsKeepSpan() {
		// 1-2 连堂 08:00-09:50 → to 的 1-2 节 08:30-10:20
		assertEquals(8 * 60 + 30 to 10 * 60 + 20, from.mapLessonMinutes(8 * 60, 9 * 60 + 50, to))
	}

	@Test
	fun endAtNextPeriodStartIsNotExtended() {
		// end 正好等于下一节开始：只算一节
		assertEquals(8 * 60 + 30 to 9 * 60 + 20, from.mapLessonMinutes(8 * 60, 9 * 60, to))
	}

	@Test
	fun beforeFirstPeriodIsUntouched() {
		assertNull(from.mapLessonMinutes(7 * 60, 7 * 60 + 40, to))
	}

	@Test
	fun outOfTargetRangeIsUntouched() {
		val shortTarget = table(8_30 to 9_20, 9_30 to 10_20)
		// from 第 3 节在 shortTarget 中不存在
		assertNull(from.mapLessonMinutes(10 * 60 + 10, 11 * 60, shortTarget))
	}

	@Test
	fun lastPeriodClampsEndIndex() {
		val shortTarget = table(8_30 to 9_20, 9_30 to 10_20, 10_30 to 11_20)
		// 1-4 连堂：目标只有 3 节，结束夹取到第 3 节
		assertEquals(8 * 60 + 30 to 11 * 60 + 20, from.mapLessonMinutes(8 * 60, 12 * 60, shortTarget))
	}

	@Test
	fun sameTableIsIdempotent() {
		assertEquals(8 * 60 to 8 * 60 + 50, from.mapLessonMinutes(8 * 60, 8 * 60 + 50, from))
		assertEquals(10 * 60 + 10 to 12 * 60, from.mapLessonMinutes(10 * 60 + 10, 12 * 60, from))
	}

	@Test
	fun emptyTableReturnsNull() {
		val empty = TimeTable(emptyArray(), emptyArray())
		assertNull(empty.mapLessonMinutes(8 * 60, 8 * 60 + 50, to))
		assertNull(from.mapLessonMinutes(8 * 60, 8 * 60 + 50, empty))
	}

	private fun table(vararg pairs: Pair<Int, Int>) = TimeTable(
		pairs.map { it.first }.toTypedArray(),
		pairs.map { it.second }.toTypedArray(),
	)
}
