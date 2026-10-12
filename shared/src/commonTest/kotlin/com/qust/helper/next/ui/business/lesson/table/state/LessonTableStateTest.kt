package com.qust.helper.next.ui.business.lesson.table.state

import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.ui.business.lesson.table.axis.hm
import com.qust.helper.next.ui.business.lesson.table.axis.lesson
import com.qust.helper.next.ui.business.lesson.table.model.LessonCandidate
import com.qust.helper.next.ui.business.lesson.table.model.LessonEntry
import com.qust.helper.next.ui.business.lesson.table.model.LessonGroup
import com.qust.helper.next.ui.business.lesson.table.model.LessonKey
import com.qust.helper.next.ui.business.lesson.table.model.LessonStatus
import com.qust.helper.next.ui.business.lesson.table.model.key
import kotlin.test.Test
import kotlin.test.assertEquals

class LessonTableStateTest {

	@Test
	fun displayedIndexFallsBackWhenKeyMissing() {
		val a = candidate("A", 1)
		val b = candidate("B", 2)
		val group = LessonGroup.create(0, listOf(a.entry, b.entry))
		val selection = mutableMapOf<Long, LessonKey>()
		assertEquals(0, displayedCandidateIndex(selection, group, listOf(a, b)))
		selection[group.id] = b.entry.key
		assertEquals(1, displayedCandidateIndex(selection, group, listOf(a, b)))
		selection[group.id] = LessonKey(999)
		assertEquals(0, displayedCandidateIndex(selection, group, listOf(a, b)))
	}

	@Test
	fun cycleWalksCandidates() {
		val a = candidate("A", 1)
		val b = candidate("B", 2)
		val c = candidate("C", 3)
		val group = LessonGroup.create(0, listOf(a.entry, b.entry, c.entry))
		val selection = mutableMapOf<Long, LessonKey>()
		var cell = LessonCellInfo(group, 0, listOf(a, b, c), 0)
		cell = cycleSelection(selection, cell)
		assertEquals(1, cell.displayedIndex)
		assertEquals("B", cell.lesson.name)
		cell = cycleSelection(selection, cell)
		assertEquals(2, cell.displayedIndex)
		cell = cycleSelection(selection, cell)
		assertEquals(0, cell.displayedIndex)
		assertEquals("A", cell.lesson.name)
	}

	@Test
	fun cycleKeepsSingleCandidate() {
		val a = candidate("A", 1)
		val group = LessonGroup.create(0, listOf(a.entry))
		val selection = mutableMapOf<Long, LessonKey>()
		val cell = LessonCellInfo(group, 0, listOf(a), 0)
		assertEquals(cell, cycleSelection(selection, cell))
		assertEquals(0, selection.size)
	}

	private fun candidate(name: String, id: Long): LessonCandidate {
		val lesson = lesson(name = name, start = hm(8, 0), end = hm(8, 50), id = id)
		return LessonCandidate(LessonEntry(lesson, lesson.key(), 0f, 1f), LessonStatus.CURRENT)
	}
}
