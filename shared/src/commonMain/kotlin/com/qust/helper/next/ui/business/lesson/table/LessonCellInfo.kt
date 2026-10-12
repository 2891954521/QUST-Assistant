package com.qust.helper.next.ui.business.lesson.table

import androidx.compose.runtime.Immutable
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.ui.business.lesson.table.model.LessonCandidate
import com.qust.helper.next.ui.business.lesson.table.model.LessonGroup

@Immutable
data class LessonCellInfo(
	val group: LessonGroup,
	val week: Int,
	val candidates: List<LessonCandidate>,
	val displayedIndex: Int,
) {
	val displayed: LessonCandidate get() = candidates[displayedIndex]
	val lesson: Lesson get() = displayed.entry.lesson
}
