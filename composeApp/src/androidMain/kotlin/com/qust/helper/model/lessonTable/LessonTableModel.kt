package com.qust.helper.model.lessonTable

import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.platform.model.lessonTable.LessonTableStorage
import com.qust.helper.room.AppDataBase
import com.qust.helper.room.entity.LessonDao
import com.qust.helper.room.entity.toLesson
import com.qust.helper.room.entity.toLessonDao

actual fun getLessonTableStorage(): LessonTableStorage = LessonTableStorageImpl()

class LessonTableStorageImpl: LessonTableStorage {

	override suspend fun getAllLesson(): List<Lesson> {
		return AppDataBase.INSTANCE.lessonDao().selectAll().map(LessonDao::toLesson)
	}

	override suspend fun saveLesson(lesson: Lesson): Boolean {
		AppDataBase.INSTANCE.lessonDao().insert(lesson.toLessonDao())
		return true
	}
}