package com.qust.helper.next.entity.lesson

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * 课表数据，是课表界面与课表组件的唯一输入模型
 * @param timeTable 节次时间表
 * @param startDay 开学时间（第一周的周一）
 * @param totalWeek 总周数
 * @param lessons 课程列表
 */
@Immutable
data class LessonTableData(
	val timeTable: TimeTable,
	val startDay: LocalDate,
	val totalWeek: Int,
	val lessons: List<Lesson> = emptyList(),
)
