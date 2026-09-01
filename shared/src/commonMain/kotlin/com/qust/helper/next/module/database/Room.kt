package com.qust.helper.next.module.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.qust.helper.next.module.database.dao.AcademicGroupDao
import com.qust.helper.next.module.database.dao.AcademicInfoDao
import com.qust.helper.next.module.database.dao.ExamDao
import com.qust.helper.next.module.database.dao.LessonDao
import com.qust.helper.next.module.database.dao.MarkDao
import com.qust.helper.next.module.database.mapper.AcademicMapper
import com.qust.helper.next.module.database.mapper.ExamMapper
import com.qust.helper.next.module.database.mapper.LessonMapper
import com.qust.helper.next.module.database.mapper.MarkMapper

expect fun createAppDataBase(): AppDataBase

@Database(
	version = 1, // 除非明确要发布版本，开发时任何数据库变动均不应该修改该版本
	exportSchema = false,
	entities = [
		LessonDao::class,
		ExamDao::class,
		MarkDao::class,
		AcademicInfoDao::class,
		AcademicGroupDao::class,
	]
)
abstract class AppDataBase : RoomDatabase() {

	companion object {
		val INSTANCE: AppDataBase by lazy { createAppDataBase() }
	}

	abstract fun lessonDao(): LessonMapper

	abstract fun examDao(): ExamMapper

	abstract fun markDao(): MarkMapper

	abstract fun academicDao(): AcademicMapper
}