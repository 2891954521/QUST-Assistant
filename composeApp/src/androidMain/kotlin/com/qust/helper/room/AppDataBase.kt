package com.qust.helper.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qust.helper.room.dao.LessonMapper
import com.qust.helper.room.entity.LessonDao


@Database(
	version = 1,
	exportSchema = false,
	entities = [
		LessonDao::class
	]
)
abstract class AppDataBase : RoomDatabase() {

	abstract fun lessonDao(): LessonMapper

	companion object {
		lateinit var INSTANCE: AppDataBase

		fun init(context: Context) {
			INSTANCE = Room.databaseBuilder(
				context.applicationContext,
				AppDataBase::class.java,
				"app_data"
			).build()
		}
	}
}