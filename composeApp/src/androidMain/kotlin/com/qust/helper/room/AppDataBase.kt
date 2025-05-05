package com.qust.helper.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qust.helper.room.dao.AcademicMapper
import com.qust.helper.room.dao.ExamMapper
import com.qust.helper.room.dao.LessonMapper
import com.qust.helper.room.dao.MarkMapper
import com.qust.helper.room.entity.AcademicGroupDao
import com.qust.helper.room.entity.AcademicInfoDao
import com.qust.helper.room.entity.ExamDao
import com.qust.helper.room.entity.LessonDao
import com.qust.helper.room.entity.MarkDao


@Database(
	version = 1,
	exportSchema = false,
	entities = [
		LessonDao::class,
		MarkDao::class,
		ExamDao::class,
		AcademicInfoDao::class,
		AcademicGroupDao::class,
	]
)
abstract class AppDataBase : RoomDatabase() {

	abstract fun lessonDao(): LessonMapper

	abstract fun markDao(): MarkMapper

	abstract fun examDao(): ExamMapper

	abstract fun academicDao(): AcademicMapper

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