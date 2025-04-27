package com.qust.helper.model.database

import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.room.AppDataBase
import com.qust.helper.room.entity.LessonDao
import com.qust.helper.room.entity.toLesson
import com.qust.helper.room.entity.toLessonDao

actual fun getLessonTableStorage(): LessonTableStorage = LessonTableStorageImpl()

class LessonTableStorageImpl: LessonTableStorage {

	override suspend fun getAllLesson(): List<Lesson> {
		return AppDataBase.INSTANCE.lessonDao().selectAll().map(LessonDao::toLesson)
	}

	override suspend fun saveLesson(lesson: Lesson): Long? {
		return AppDataBase.INSTANCE.lessonDao().insert(lesson.toLessonDao())
	}

	override suspend fun updateLesson(lesson: Lesson): Boolean {
		return AppDataBase.INSTANCE.lessonDao().update(lesson.toLessonDao()) == 1
	}

	override suspend fun mergeLesson(new: List<Lesson>, update: List<Lesson>, delete: List<Lesson>): Boolean {
		try{
			AppDataBase.INSTANCE.lessonDao().mergeLesson(new.map(Lesson::toLessonDao), update.map(Lesson::toLessonDao), delete.map(Lesson::toLessonDao))
			return true
		}catch(e: Exception){
			return false
		}
	}

}