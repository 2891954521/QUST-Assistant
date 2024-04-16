package com.qust.helper.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Mark::class, LessonInfo::class, LessonInfoGroup::class], version = 1, exportSchema = false)
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
//				.addMigrations(object : Migration(1, 2) {
//					override fun migrate(db: SupportSQLiteDatabase) {
//						db.execSQL("ALTER TABLE marks ADD COLUMN time TEXT NOT NULL DEFAULT \"\"")
//					}
//				})
				.build()
				INSTANCE = instance
				instance
			}
		}
	}
}