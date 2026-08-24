package com.qust.helper.next.entity.lesson

import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.next.repository.SettingRepository
import kotlinx.datetime.LocalDate

data class LessonTableInfo(
	/** 时间表 */
	val timeTable: TimeTable = TimeTable.DEFAULT,
	/** 开学时间 */
	val startDay: LocalDate = SettingRepository.startDay,
	/** 总周数 */
	val totalWeek: Int = SettingRepository.totalWeek,
	/** 当前周 (从 0 开始) */
	val currentWeek: Int = 0,

	val lessons: List<Lesson> = emptyList()
)