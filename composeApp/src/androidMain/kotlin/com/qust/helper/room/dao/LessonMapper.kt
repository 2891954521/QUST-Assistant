package com.qust.helper.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.qust.helper.room.entity.LessonDao

@Dao
interface LessonMapper {

	@Query("SELECT * FROM lesson")
	fun selectAll(): List<LessonDao>

	@Insert
	fun insert(lesson: LessonDao)

	@Insert
	fun insertAll(lessons: List<LessonDao>)

	@Update
	fun update(lesson: LessonDao)

	@Query("DELETE FROM lesson WHERE 1")
	fun clearTable()
}