package com.qust.helper.next.network.entity.lesson

import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.next.utils.DateUtils
import kotlinx.datetime.LocalDate

/**
 * 查询课表完成后的结果
 * @param error 错误消息
 * @param termText 学期文本
 * @param lessons 课表
 */
data class LessonTableQueryResult(
	var error: String? = null,
	var termText: String = "",
	var startDay: LocalDate = DateUtils.currentDate(),
	var totalWeek: Int = 1,
	var lessons: List<Lesson>? = null
)