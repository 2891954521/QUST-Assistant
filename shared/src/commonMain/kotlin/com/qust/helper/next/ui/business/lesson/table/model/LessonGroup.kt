package com.qust.helper.next.ui.business.lesson.table.model

import androidx.compose.runtime.Immutable
import com.qust.helper.next.repository.LessonTableRepository

enum class LessonStatus { CURRENT, UPCOMING, FINISHED }

@Immutable
data class LessonCandidate(
	val entry: LessonEntry,
	val status: LessonStatus,
)

@Immutable
class LessonGroup(
	val id: Long,
	val dayOfWeek: Int,
	val entries: List<LessonEntry>,
	val startOffset: Float,
	val endOffset: Float,
) {
	fun resolve(week: Int, settings: LessonTableRepository.LessonTableSetting): List<LessonCandidate> {
		val current = entries.filter { it.hasWeek(week) }
		if(current.isNotEmpty()) {
			return current
				.sortedWith(compareBy({ it.lesson.startMinute }, { it.key.value }))
				.map { LessonCandidate(it, LessonStatus.CURRENT) }
		}
		if(!settings.showNonCurrentWeek) return emptyList()

		val upcoming = entries
			.filter { it.firstWeekAfter(week) != null }
			.sortedBy { it.firstWeekAfter(week) }
			.map { LessonCandidate(it, LessonStatus.UPCOMING) }

		if(!settings.showFinished) return upcoming

		val upcomingKeys = upcoming.mapTo(HashSet()) { it.entry.key }
		val finished = entries
			.filter { it.lastWeekBefore(week) != null && it.key !in upcomingKeys }
			.sortedByDescending { it.lastWeekBefore(week) }
			.map { LessonCandidate(it, LessonStatus.FINISHED) }

		return if(finished.isEmpty()) upcoming else upcoming + finished
	}

	companion object {
		fun create(dayOfWeek: Int, entries: List<LessonEntry>): LessonGroup {
			val start = entries.minOf { it.startOffset }
			val end = entries.maxOf { it.endOffset }
			return LessonGroup(
				id = lessonGroupId(dayOfWeek, entries),
				dayOfWeek = dayOfWeek,
				entries = entries,
				startOffset = start,
				endOffset = end,
			)
		}

		internal fun lessonGroupId(dayOfWeek: Int, entries: List<LessonEntry>): Long {
			var hash = dayOfWeek.toLong()
			for(entry in entries) {
				hash = hash * 31 + entry.key.value
			}
			return hash
		}
	}
}
