package com.qust.helper.next.module.database.mapper

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.qust.helper.next.module.database.dao.LessonDao

@Dao
interface LessonMapper {

	@Query("SELECT * FROM lesson")
	suspend fun selectAll(): List<LessonDao>

	@Insert
	suspend fun insert(lesson: LessonDao): Long?

	@Insert
	suspend fun insertAll(lessons: List<LessonDao>)

	@Update
	suspend fun update(lesson: LessonDao): Int

	@Update
	suspend fun updateAll(lessons: List<LessonDao>): Int

	@Delete
	suspend fun delete(lesson: LessonDao)

	@Delete
	suspend fun deleteAll(lessons: List<LessonDao>)


	@Query("DELETE FROM lesson WHERE 1")
	suspend fun clearTable()

	@Transaction
	suspend fun mergeLesson(new: List<LessonDao>, update: List<LessonDao>, delete: List<LessonDao>) {
		insertAll(new)
		assert(updateAll(update) == update.size)
		deleteAll(delete)
	}
}