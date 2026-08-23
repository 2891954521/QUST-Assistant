package com.qust.helper.entity.business

import kotlinx.serialization.Serializable

/**
 * 体测 - 学年信息
 */
@Serializable
data class SportYear(
	val text: String = "",
	val value: Int = 0,
)

/**
 * 体测 - 学年任务
 */
@Serializable
data class SportTask(
	val name: String = "",
	val testType: Int = 0,
	val id: Int = 0,
)

/**
 * 体测 - 成绩明细
 */
@Serializable
data class SportScore(
	val avg: String = "",
	val score2: String = "",
	val isCheat: Boolean = false,
	val level: String = "",
	val score1: String = "",
	val projectName: String = "",
	val percent: String = "",
)

/**
 * 体测 - 成绩上传记录
 */
@Serializable
data class SportUpload(
	val studentId: Int = 0,
	val score: String = "",
	val uploadId: Int = 0,
	val studentNumber: String = "",
	val projectNumber: Int = 0,
	val testTime: Long = 0,
	val projectName: String = "",
	val taskId: Int = 0,
)
