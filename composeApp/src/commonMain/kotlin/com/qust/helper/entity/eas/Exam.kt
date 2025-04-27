package com.qust.helper.entity.eas

import kotlinx.serialization.Serializable

/**
 * 考试安排
 * @param name  课程名称
 * @param place 考试地点
 * @param time  考试时间
 */
@Serializable
data class Exam(
	val name: String = "",
	val place: String = "",
	val time: String = "",
) {

}