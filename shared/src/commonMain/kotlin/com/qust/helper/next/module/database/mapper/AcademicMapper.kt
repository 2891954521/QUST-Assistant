package com.qust.helper.next.module.database.mapper

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.qust.helper.next.module.database.dao.AcademicGroupDao
import com.qust.helper.next.module.database.dao.AcademicInfoDao

@Dao
interface AcademicMapper {

	@Query("SELECT * FROM academic_info")
	suspend fun selectInfo(): List<AcademicInfoDao>

	@Query("SELECT * FROM academic_group")
	suspend fun selectGroups(): List<AcademicGroupDao>

	@Query("SELECT * FROM academic_info WHERE term_index = :term")
	suspend fun selectByTerm(term: Int): List<AcademicInfoDao>

	@Query("SELECT * FROM academic_info WHERE lesson_group = :group")
	suspend fun selectByGroup(group: Int): List<AcademicInfoDao>

	@Query(
		"""
		SELECT
			term_index AS lesson_group,
			'' AS type,
			COUNT(1) AS totalCounts,
			SUM(credit) AS requireCredits,
			SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS passedCounts,
			SUM(CASE WHEN status = 4 THEN credit ELSE 0 END) AS obtainedCredits,
			SUM(CASE WHEN status != 4 THEN credit ELSE 0 END) AS creditNotEarned
		FROM academic_info
		GROUP BY term_index
		"""
	)
	suspend fun selectGroupByTerm(): List<AcademicGroupDao>

	@Insert
	suspend fun insertInfo(info: List<AcademicInfoDao>)

	@Insert
	suspend fun insertGroups(groups: List<AcademicGroupDao>)

	@Query("DELETE FROM academic_info")
	suspend fun clearInfo()

	@Query("DELETE FROM academic_group")
	suspend fun clearGroups()
}
