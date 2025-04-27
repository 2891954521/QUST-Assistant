package com.qust.helper.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.qust.helper.room.entity.ExamDao

@Dao
interface ExamMapper {

	@Query("SELECT * FROM exams WHERE `term` = :term")
	fun selectByTerm(term: Int): List<ExamDao>

	@Query("UPDATE exams SET isNew = 0 WHERE id = :id")
	fun setRead(id: Int)

	@Insert
	fun insert(lesson: ExamDao)

	@Insert
	fun insertAll(lessons: List<ExamDao>)

	@Update
	fun update(lesson: ExamDao)

	@Update
	fun updateAll(lesson: List<ExamDao>)

	@Query("DELETE FROM exams WHERE `term` = :term")
	fun clear(term: Int)

	@Query("DELETE FROM exams")
	fun delete()
}