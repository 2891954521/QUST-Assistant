package com.qust.helper.next.ui.business.lesson.table.model

import com.qust.helper.next.entity.lesson.Lesson

@JvmInline
value class LessonKey(val value: Long)

fun Lesson.key(): LessonKey {
	if(id != 0L) return LessonKey(id)
	return LessonKey(contentHash() or (1L shl 62))
}

private fun Lesson.contentHash(): Long {
	var hash = week.toLong()
	hash = hash * 31 + startMinute
	hash = hash * 31 + endMinute
	hash = hash * 31 + name.hashCode()
	hash = hash * 31 + place.hashCode()
	hash = hash * 31 + teacher.hashCode()
	hash = hash * 31 + weeks
	return hash
}
