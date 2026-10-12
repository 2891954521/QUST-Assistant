package com.qust.helper.next.ui.business.lesson.table.model

import com.qust.helper.next.ui.business.lesson.table.axis.hm
import com.qust.helper.next.ui.business.lesson.table.axis.lesson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LessonKeyTest {

	@Test
	fun usesIdWhenPresent() {
		val lesson = lesson(name = "课", start = hm(8, 0), end = hm(8, 50), id = 42)
		assertEquals(LessonKey(42), lesson.key())
	}

	@Test
	fun sameContentSameSyntheticKey() {
		val a = lesson(name = "课", start = hm(8, 0), end = hm(8, 50), place = "A", teacher = "T")
		val b = lesson(name = "课", start = hm(8, 0), end = hm(8, 50), place = "A", teacher = "T")
		assertEquals(a.key(), b.key())
		assertTrue(a.key().value and (1L shl 62) != 0L)
	}

	@Test
	fun differentContentDifferentKey() {
		val a = lesson(name = "课A", start = hm(8, 0), end = hm(8, 50))
		val b = lesson(name = "课B", start = hm(8, 0), end = hm(8, 50))
		assertNotEquals(a.key(), b.key())
	}

	@Test
	fun syntheticKeyDoesNotCollideWithRealId() {
		val real = lesson(name = "课", start = hm(8, 0), end = hm(8, 50), id = 1)
		val synthetic = lesson(name = "课", start = hm(8, 0), end = hm(8, 50), id = 0)
		assertNotEquals(real.key(), synthetic.key())
		assertEquals(0L, real.key().value and (1L shl 62))
	}
}
