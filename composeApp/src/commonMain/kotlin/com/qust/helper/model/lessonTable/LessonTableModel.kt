package com.qust.helper.model.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.model.database.LessonTableStorage
import com.qust.helper.model.database.getLessonTableStorage
import com.qust.helper.model.eas.LessonTableQueryResult
import com.qust.helper.utils.LessonUtils

object LessonTableModel: LessonTableStorage by getLessonTableStorage() {

	/** 时间表 */
	val _timeTable = mutableStateOf(TimeTable.DEFAULT)
	val timeTable by _timeTable

	/** 开学时间 */
	val _startDay = mutableStateOf(SettingModel.startDay)
	val startDay by _startDay

	/** 总周数 */
	val _totalWeek = mutableIntStateOf(SettingModel.totalWeek)
	val totalWeek by _totalWeek

	/** 当前周 (从 0 开始) */
	var _currentWeek = mutableIntStateOf(0)
	val currentWeek by _currentWeek

	/** 当前星期 ( 0 - 6, 周一 —— 周日) */
	var _dayOfWeek = mutableIntStateOf(0)
	val dayOfWeek by _dayOfWeek


	suspend fun saveLessonTable(result: LessonTableQueryResult){
		val lessons = result.lessons ?: return

		val lessonChange = LessonUtils.mergeLesson(lessons, getAllLesson())

		mergeLesson(lessonChange.first, lessonChange.second, lessonChange.third)

		_startDay.value = result.startDay
		_totalWeek.value = result.totalWeek

		SettingModel.startDay = result.startDay
		SettingModel.totalWeek = result.totalWeek
	}

}