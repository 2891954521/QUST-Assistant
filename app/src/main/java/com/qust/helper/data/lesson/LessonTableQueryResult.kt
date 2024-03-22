package com.qust.helper.data.lesson

/**
 * 查询课表完成后的结果
 * @param error 错误消息
 * @param termText 学期文本
 * @param lessonTable 课表
 */
data class LessonTableQueryResult(
	var error: String? = null,
	var termText: String = "",
	var lessonTable: LessonTable = LessonTable()
)