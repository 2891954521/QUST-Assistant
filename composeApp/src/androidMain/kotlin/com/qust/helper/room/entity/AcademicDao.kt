package com.qust.helper.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qust.helper.entity.eas.AcademicGroup
import com.qust.helper.entity.eas.AcademicInfo

/**
 * 学业情况查询课程
 *
 * @param kchId 课程号
 * @param name 课程名称
 * @param type 课程类型
 *
 * @param status 修读状态
 *
 * @param credit 学分
 * @param mark 成绩
 * @param gpa 绩点
 *
 * @param category 课程类别名称
 * @param content 课程组成
 *
 * @param index 学年学期索引
 */
@Entity(tableName = "academic_info")
data class AcademicInfoDao(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,

	@ColumnInfo val kchId: String,
	@ColumnInfo val name: String,
	@ColumnInfo val type: String,

	@ColumnInfo val credit: Float,
	@ColumnInfo val mark: String,
	@ColumnInfo val gpa: Float,

	@ColumnInfo val status: Int = 0,

	@ColumnInfo val category: String,
	@ColumnInfo val content: String,

	@ColumnInfo val index: Int,
	@ColumnInfo val group: Int,
)


/**
 * @param group 课程分组
 * @param type 课程性质名称
 * @param requireCredits 要求学分
 * @param obtainedCredits 已获得学分
 * @param creditNotEarned 未通过学分
 * @param passedCounts 已通过门数
 * @param totalCounts 总共门数
 */
@Entity(tableName = "academic_group")
data class AcademicGroupDao(
	@PrimaryKey val group: Int,
	@ColumnInfo val type: String = "",
	@ColumnInfo val requireCredits: Float = 0f,
	@ColumnInfo val obtainedCredits: Float = 0f,
	@ColumnInfo val creditNotEarned: Float = 0f,
	@ColumnInfo val passedCounts: Int = 0,
	@ColumnInfo val totalCounts: Int = 0
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
	group = this.group
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
	group = this.group
)

fun AcademicGroupDao.toAcademicGroup() = AcademicGroup(
	group = this.group,
	type = this.type,
	requireCredits = this.requireCredits,
	obtainedCredits = this.obtainedCredits,
	creditNotEarned = this.creditNotEarned,
	passedCounts = this.passedCounts,
	totalCounts = this.totalCounts
)

fun AcademicGroup.toAcademicGroupDao() = AcademicGroupDao(
	group = this.group,
	type = this.type,
	requireCredits = this.requireCredits,
	obtainedCredits = this.obtainedCredits,
	creditNotEarned = this.creditNotEarned,
	passedCounts = this.passedCounts,
	totalCounts = this.totalCounts
)