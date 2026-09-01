package com.qust.helper.next.module.database.dao

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.qust.helper.next.entity.eas.AcademicGroup
import com.qust.helper.next.entity.eas.AcademicInfo

@Entity(tableName = "academic_info")
class AcademicInfoDao(

	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,

	@ColumnInfo val kchId: String = "",
	@ColumnInfo val name: String = "",
	@ColumnInfo val type: String = "",

	@ColumnInfo val credit: Float = 0F,
	@ColumnInfo val mark: String = "",
	@ColumnInfo val gpa: Float = 0F,

	@ColumnInfo val status: Int = 0,

	@ColumnInfo val category: String = "",
	@ColumnInfo val content: String = "",

	@ColumnInfo(name = "term_index") val index: Int = 0,
	@ColumnInfo(name = "lesson_group") val group: Int = 0,
)

@Entity(tableName = "academic_group")
class AcademicGroupDao(

	@PrimaryKey
	@ColumnInfo(name = "lesson_group")
	val group: Int = 0,

	@ColumnInfo val type: String = "",
	@ColumnInfo val requireCredits: Float = 0f,
	@ColumnInfo val obtainedCredits: Float = 0f,
	@ColumnInfo val creditNotEarned: Float = 0f,
	@ColumnInfo val passedCounts: Int = 0,
	@ColumnInfo val totalCounts: Int = 0,
)

fun AcademicInfoDao.toAcademicInfo() = AcademicInfo(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	mark = this.mark,
	gpa = this.gpa,
	status = this.status,
	category = this.category,
	content = this.content,
	index = this.index,
	group = this.group,
)

fun AcademicInfo.toAcademicInfoDao() = AcademicInfoDao(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	mark = this.mark,
	gpa = this.gpa,
	status = this.status,
	category = this.category,
	content = this.content,
	index = this.index,
	group = this.group,
)

fun AcademicGroupDao.toAcademicGroup() = AcademicGroup(
	group = this.group,
	type = this.type,
	requireCredits = this.requireCredits,
	obtainedCredits = this.obtainedCredits,
	creditNotEarned = this.creditNotEarned,
	passedCounts = this.passedCounts,
	totalCounts = this.totalCounts,
)

fun AcademicGroup.toAcademicGroupDao() = AcademicGroupDao(
	group = this.group,
	type = this.type,
	requireCredits = this.requireCredits,
	obtainedCredits = this.obtainedCredits,
	creditNotEarned = this.creditNotEarned,
	passedCounts = this.passedCounts,
	totalCounts = this.totalCounts,
)
