package com.qust.helper.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.qust.helper.room.entity.AcademicGroupDao
import com.qust.helper.room.entity.AcademicInfoDao

@Dao
interface AcademicMapper {

	@Query("SELECT * FROM academic_info")
	fun selectInfo(): List<AcademicInfoDao>

	@Query("SELECT * FROM academic_group")
	fun selectGroups(): List<AcademicGroupDao>

	@Query("SELECT * FROM academic_info WHERE `index` = :term")
	fun selectByTerm(term: Int): List<AcademicInfoDao>

	@Query("SELECT * FROM academic_info WHERE `group` = :group")
	fun selectByGroup(group: Int): List<AcademicInfoDao>

	@Query("""SELECT
`index` AS 'group',
'' AS type,
COUNT(1) AS totalCounts,
SUM(credit) AS requireCredits,
SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS passedCounts,
SUM(CASE WHEN status = 4 THEN credit ELSE 0 END) AS obtainedCredits,
SUM(CASE WHEN status != 4 THEN credit ELSE 0 END) AS creditNotEarned
FROM academic_info GROUP BY `index`
""")
	fun selectGroupByTerm(): List<AcademicGroupDao>


	@Insert
	fun insertInfo(lessons: List<AcademicInfoDao>)

	@Insert
	fun insertGroups(lessons: List<AcademicGroupDao>)

	@Query("DELETE FROM academic_info")
	fun clearInfo()

	@Query("DELETE FROM academic_group")
	fun clearGroups()
}

