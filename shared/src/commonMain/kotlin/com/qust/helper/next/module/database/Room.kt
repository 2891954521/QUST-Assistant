package com.qust.helper.next.module.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.qust.helper.next.module.database.dao.LessonDao
import com.qust.helper.next.module.database.mapper.LessonMapper

expect fun createAppDataBase(): AppDataBase

@Database(
	version = 1,
	exportSchema = false,
	entities = [
		LessonDao::class
	]
)
abstract class AppDataBase : RoomDatabase() {

	companion object {
		val INSTANCE: AppDataBase by lazy { createAppDataBase() }
	}

	abstract fun lessonDao(): LessonMapper
}