package com.qust.helper.next.module.database.mapper

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.qust.helper.next.module.database.dao.MarkDao

@Dao
interface MarkMapper {

	@Query("SELECT * FROM marks WHERE `term` = :term")
	suspend fun selectByTerm(term: Int): List<MarkDao>

	@Query("UPDATE marks SET isNew = 0 WHERE id = :id")
	suspend fun setRead(id: Int)

	@Insert
	suspend fun insert(mark: MarkDao)

	@Insert
	suspend fun insertAll(marks: List<MarkDao>)

	@Update
	suspend fun update(mark: MarkDao)

	@Update
	suspend fun updateAll(marks: List<MarkDao>)

	@Query("DELETE FROM marks WHERE `term` = :term")
	suspend fun clear(term: Int)

	@Query("DELETE FROM marks")
	suspend fun delete()
}
