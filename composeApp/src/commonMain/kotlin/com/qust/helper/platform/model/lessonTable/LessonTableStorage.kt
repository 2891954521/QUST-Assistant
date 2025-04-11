package com.qust.helper.platform.model.lessonTable

import com.qust.helper.entity.lesson.Lesson

interface LessonTableStorage {

	suspend fun getAllLesson(): List<Lesson> {
		return emptyList()
	}

	suspend fun saveLesson(lesson: Lesson): Boolean {
		return false
	}
}