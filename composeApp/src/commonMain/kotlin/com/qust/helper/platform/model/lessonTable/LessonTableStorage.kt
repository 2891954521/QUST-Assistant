package com.qust.helper.platform.model.lessonTable

import com.qust.helper.entity.lesson.Lesson

interface LessonTableStorage {

	suspend fun getAllLesson(): List<Lesson> {
		return emptyList()
	}

	suspend fun saveLesson(lesson: Lesson): Long? {
		return -1
	}

	suspend fun updateLesson(lesson: Lesson): Boolean {
		return false
	}

	suspend fun mergeLesson(new: List<Lesson>, update: List<Lesson>, delete: List<Lesson>): Boolean {
		return false
	}
}