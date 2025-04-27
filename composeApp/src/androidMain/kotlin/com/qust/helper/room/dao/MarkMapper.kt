package com.qust.helper.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.qust.helper.room.entity.MarkDao

@Dao
interface MarkMapper {

	@Query("SELECT * FROM marks WHERE `kchId` = :id")
	fun selectByKchId(id: String): List<MarkDao>

	@Query("SELECT * FROM marks WHERE `index` = :index")
	fun selectByIndex(index: Int): List<MarkDao>

	@Query("UPDATE marks SET isNew = 0 WHERE id = :id")
	fun setRead(id: Int)

	@Query("SELECT COUNT(1) FROM marks WHERE `index` = :index")
	fun countByIndex(index: Int): Int

	@Insert
	fun insert(lesson: MarkDao)

	@Insert
	fun insertAll(lessons: List<MarkDao>)

	@Update
	fun update(lesson: MarkDao)

	@Update
	fun updateAll(lesson: List<MarkDao>)

	@Query("DELETE FROM marks WHERE `index` = :index")
	fun clear(index: Int)

	@Query("DELETE FROM marks")
	fun delete()
}