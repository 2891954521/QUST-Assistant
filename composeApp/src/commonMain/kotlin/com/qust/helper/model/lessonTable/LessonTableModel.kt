package com.qust.helper.model.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.entity.lesson.TimeTable
import java.util.Date

object LessonTableModel {

	/** 时间表 */
	val _timeTable = mutableStateOf(TimeTable.DEFAULT)
	val timeTable by _timeTable

	/** 开学时间 */
	val _startDay = mutableStateOf(Date())
	val startDay by _startDay

	/** 总周数 */
	val _totalWeek = mutableIntStateOf(1)
	val totalWeek by _totalWeek

	/** 当前周 (从 0 开始) */
	var _currentWeek = mutableIntStateOf(0)
	val currentWeek by _currentWeek

	/** 当前星期 ( 0 - 6, 周一 —— 周日) */
	var _dayOfWeek = mutableIntStateOf(0)
	val dayOfWeek by _dayOfWeek
}