package com.qust.helper.next.entity.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTableValidatorTest {

	@Test
	fun winterPresetIsValid() {
		val winter = TimeTablePresets.WINTER_NEW
		assertNull(TimeTableValidator.validate(winter.startTime, winter.endTime))
	}

	@Test
	fun sizeMismatch() {
		val issue = TimeTableValidator.validate(listOf(800), listOf(845, 945))!!
		assertEquals(-1, issue.periodIndex)
	}

	@Test
	fun periodCountOutOfRange() {
		assertEquals(-1, TimeTableValidator.validate(emptyList(), emptyList())!!.periodIndex)

		val count = TimeTableValidator.MAX_PERIODS + 1
		val start = List(count) { 800 }
		val end = List(count) { 845 }
		assertEquals(-1, TimeTableValidator.validate(start, end)!!.periodIndex)
	}

	@Test
	fun invalidMinuteAndHour() {
		assertEquals(0, TimeTableValidator.validate(listOf(860), listOf(900))!!.periodIndex)
		assertEquals(1, TimeTableValidator.validate(listOf(800, 2400), listOf(845, 2450))!!.periodIndex)
		assertEquals(0, TimeTableValidator.validate(listOf(800), listOf(870))!!.periodIndex)
	}

	@Test
	fun endMustBeAfterStart() {
		assertEquals(0, TimeTableValidator.validate(listOf(900), listOf(845))!!.periodIndex)
		assertEquals(0, TimeTableValidator.validate(listOf(900), listOf(900))!!.periodIndex)
	}

	@Test
	fun periodsMustNotOverlap() {
		assertEquals(1, TimeTableValidator.validate(listOf(800, 830), listOf(900, 930))!!.periodIndex)
		// 结束与下一节开始重合是允许的（连堂）
		assertNull(TimeTableValidator.validate(listOf(800, 845), listOf(845, 930)))
	}

	@Test
	fun timeHelpers() {
		assertEquals(8 * 60, TimeTableValidator.toMinute(800))
		assertEquals(8 * 60 + 45, TimeTableValidator.toMinute(845))
		assertEquals(-1, TimeTableValidator.toMinute(860))

		assertTrue(TimeTableValidator.isValidTime(0))
		assertTrue(TimeTableValidator.isValidTime(2359))
		assertTrue(!TimeTableValidator.isValidTime(2400))
		assertTrue(!TimeTableValidator.isValidTime(-1))
	}
}
