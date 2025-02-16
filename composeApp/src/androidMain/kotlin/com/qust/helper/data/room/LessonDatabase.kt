package com.qust.helper.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL("""CREATE TABLE IF NOT EXISTS `lesson_info`(
			`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
			`kchId` TEXT NOT NULL,
			`name` TEXT NOT NULL,
			`type` TEXT NOT NULL,
			`credit` REAL NOT NULL,
			`mark` TEXT NOT NULL,
			`gpa` REAL NOT NULL,
			`status` INTEGER NOT NULL,
			`category` TEXT NOT NULL,
			`content` TEXT NOT NULL,
			`index` INTEGER NOT NULL,
			`group` INTEGER NOT NULL
		)""")

		db.execSQL("""CREATE TABLE IF NOT EXISTS `lesson_info_group`(
			`group` INTEGER NOT NULL,
			`type` TEXT NOT NULL,
			`requireCredits` REAL NOT NULL,
			`obtainedCredits` REAL NOT NULL,
			`creditNotEarned` REAL NOT NULL,
			`passedCounts` INTEGER NOT NULL,
			`totalCounts` INTEGER NOT NULL,
			PRIMARY KEY(`group`)
		)""")

	}
}

@Database(entities = [Mark::class, LessonInfo::class, LessonInfoGroup::class], version = 2, exportSchema = false)
abstract class LessonDatabase : RoomDatabase() {

	abstract fun markDao(): MarkDao

	abstract fun lessonInfoDao(): LessonInfoDao

	companion object {
		@Volatile
		private var INSTANCE: LessonDatabase? = null

		fun getInstance(context: Context): LessonDatabase {
			return INSTANCE ?: synchronized(this) {
				val instance = Room.databaseBuilder(
					context.applicationContext,
					LessonDatabase::class.java,
					"lessons"
				)
				.addMigrations(MIGRATION_1_2)
				.build()
				INSTANCE = instance
				instance
			}
		}
	}
}