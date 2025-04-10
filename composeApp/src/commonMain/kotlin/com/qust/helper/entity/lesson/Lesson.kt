package com.qust.helper.entity.lesson

import kotlinx.serialization.Serializable

/**
 * 一节课程
 * @param id 数据库ID
 * @param type 课程类型 0: auto 自动添加的课程; 1: user 用户创建的课程
 * @param reference 对另一个课程的引用，修改该课程会影响本课程
 * @param lessonId 课程ID，用于自动创建的课程标识唯一ID
 * @param colorLabel 课程颜色，内置颜色的索引
 * @param weeks 第几周有课, long形式的boolean数组
 * @param week 周几
 * @param startMinute 上课时间
 * @param endMinute 下课时间
 * @param name 课程名称
 * @param place 课程教室
 * @param teacher 课程教师
 * @param remark 备注
 */
@Serializable
open class Lesson(
	val id: Long = 0L,

	val type: Int = 0,
	val reference: Long = 0L,

	val lessonId: String = "",

	val colorLabel: Int = 0,

	val weeks: Long = 0L,
	val week: Int = 0,
	val startMinute: Int = 0,
	val endMinute: Int = 0,

	val name: String = "",
	val place: String = "",
	val teacher: String = "",

	val remark: String = ""
)