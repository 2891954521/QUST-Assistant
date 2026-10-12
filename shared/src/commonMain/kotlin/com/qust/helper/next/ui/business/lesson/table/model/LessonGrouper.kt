package com.qust.helper.next.ui.business.lesson.table.model

fun interface LessonGrouper {
	fun group(entries: List<LessonEntry>): List<LessonGroup>
}

object OverlapLessonGrouper : LessonGrouper {
	override fun group(entries: List<LessonEntry>): List<LessonGroup> {
		if(entries.isEmpty()) return emptyList()
		val sorted = entries.sortedWith(
			compareBy<LessonEntry> { it.lesson.startMinute }
				.thenByDescending { it.lesson.endMinute }
		)
		val groups = ArrayList<LessonGroup>()
		var cluster = ArrayList<LessonEntry>()
		cluster += sorted[0]
		var maxEnd = sorted[0].lesson.endMinute
		for(i in 1 until sorted.size) {
			val entry = sorted[i]
			if(entry.lesson.startMinute < maxEnd) {
				cluster += entry
				if(entry.lesson.endMinute > maxEnd) maxEnd = entry.lesson.endMinute
			} else {
				groups += LessonGroup.create(cluster.first().lesson.week, cluster)
				cluster = ArrayList()
				cluster += entry
				maxEnd = entry.lesson.endMinute
			}
		}
		groups += LessonGroup.create(cluster.first().lesson.week, cluster)
		return groups
	}
}
