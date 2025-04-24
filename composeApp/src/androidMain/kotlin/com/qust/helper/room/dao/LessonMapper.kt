package com.qust.helper.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.qust.helper.room.entity.LessonDao

@Dao
interface LessonMapper {

	@Query("SELECT * FROM lesson")
	fun selectAll(): List<LessonDao>

	@Insert
	fun insert(lesson: LessonDao): Long?

	@Insert
	fun insertAll(lessons: List<LessonDao>)

	@Update
	fun update(lesson: LessonDao): Int

	@Update
	fun updateAll(lessons: List<LessonDao>): Int

	@Delete
	fun delete(lesson: LessonDao)

	@Delete
	fun deleteAll(lessons: List<LessonDao>)


	@Query("DELETE FROM lesson WHERE 1")
	fun clearTable()

	@Transaction
	open fun mergeLesson(new: List<LessonDao>, update: List<LessonDao>, delete: List<LessonDao>) {
		insertAll(new)
		assert(updateAll(update) == update.size)
		deleteAll(delete)
	}
}