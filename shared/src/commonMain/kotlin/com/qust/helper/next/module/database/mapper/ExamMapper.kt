package com.qust.helper.next.module.database.mapper

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.qust.helper.next.module.database.dao.ExamDao

@Dao
interface ExamMapper {

	@Query("SELECT * FROM exams WHERE `term` = :term")
	suspend fun selectByTerm(term: Int): List<ExamDao>

	@Query("UPDATE exams SET isNew = 0 WHERE id = :id")
	suspend fun setRead(id: Int)

	@Insert
	suspend fun insert(exam: ExamDao)

	@Insert
	suspend fun insertAll(exams: List<ExamDao>)

	@Update
	suspend fun update(exam: ExamDao)

	@Update
	suspend fun updateAll(exams: List<ExamDao>)

	@Query("DELETE FROM exams WHERE `term` = :term")
	suspend fun clear(term: Int)

	@Query("DELETE FROM exams")
	suspend fun delete()
}
