package com.qust.helper.ui.widget.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.entity.lesson.TimeTable
import java.util.Date

class LessonTableUIState {

	val timeTable by mutableStateOf(TimeTable.DEFAULT)

	val startDay by mutableStateOf(Date())

	val currentWeek by mutableStateOf(0)

	val totalWeek by mutableStateOf(20)

	val lessonTable = listOf<LessonGroupRenderAble>().toMutableList()

}

data class LessonRenderAble(
	val name: String = "",
	val place: String = "",
	val teacher: String = "",
)

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 * @param week 星期几 1-7
 * @param startTime 上课时间
 * @param endTime 下课时间
 */
class LessonGroupRenderAble(
	val week: Int,
	val startTime: Int,
	val endTime: Int,
	val lessons: Array<LessonRenderAble> = emptyArray()
)
