package com.qust.helper.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Keys
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.model.database.LessonTableStorage
import com.qust.helper.model.database.getLessonTableStorage
import com.qust.helper.model.eas.LessonTableQueryResult
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableInfo
import com.qust.helper.utils.LessonUtils
import com.qust.helper.utils.SettingUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

object LessonTableRepository: LessonTableStorage by getLessonTableStorage() {

	/** 显示非本周课程 */
	val _showAllLesson = mutableStateOf(SettingUtils[Keys.KEY_SHOW_ALL_LESSON, true])
	val showAllLesson by _showAllLesson
	fun setShowAllLessonValue(value: Boolean) {
		_showAllLesson.value = value
		SettingUtils[Keys.KEY_SHOW_ALL_LESSON] = value
	}

	/** 隐藏后续无课课程 */
	val _hideFinishLesson = mutableStateOf(SettingUtils[Keys.KEY_HIDE_FINISH_LESSON, false])
	val hideFinishLesson by _hideFinishLesson
	fun setHideFinishLessonValue(value: Boolean) {
		_hideFinishLesson.value = value
		SettingUtils[Keys.KEY_HIDE_FINISH_LESSON] = value
	}

	/** 隐藏教师 */
	val _hideTeacher = mutableStateOf(SettingUtils[Keys.KEY_HIDE_TEACHER, false])
	val hideTeacher by _hideTeacher
	fun setHideTeacherValue(value: Boolean) {
		_hideTeacher.value = value
		SettingUtils[Keys.KEY_HIDE_TEACHER] = value
	}

	/** 锁定课表 */
	val _lockLesson = mutableStateOf(SettingUtils[Keys.KEY_LOCK_LESSON, false])
	val lockLesson by _lockLesson
	fun setLockLessonValue(value: Boolean) {
		_lockLesson.value = value
		SettingUtils[Keys.KEY_LOCK_LESSON] = value
	}

	/** 当前时间表 0: 冬季 1: 夏季 */
	val _currentTimeTable = mutableIntStateOf(SettingUtils[Keys.KEY_TIME_TABLE, 0])
	val currentTimeTable by _currentTimeTable
	fun setTimeTableValue(value: Int) {
		_currentTimeTable.value = value
		SettingUtils[Keys.KEY_TIME_TABLE] = value
		refreshTimeTable()
	}

	/** 使用高密时间表 */
	val _gaomiTimeTable = mutableStateOf(SettingUtils[Keys.KEY_GAOMI_TIME_TABLE, false])
	val gaomiTimeTable by _gaomiTimeTable
	fun setGaomiTimeTable(value: Boolean) {
		_gaomiTimeTable.value = value
		SettingUtils[Keys.KEY_GAOMI_TIME_TABLE] = value
		refreshTimeTable()
	}

	/** 根据当前设置解析时间表 */
	fun resolveTimeTable(): TimeTable {
		return when {
			gaomiTimeTable && currentTimeTable == 1 -> TimeTable.Companion.GAOMI_SUMMER
			gaomiTimeTable -> TimeTable.Companion.GAOMI_WINTER
			currentTimeTable == 1 -> TimeTable.Companion.SUMMER
			else -> TimeTable.Companion.DEFAULT
		}
	}

	private fun refreshTimeTable() {
		_currentLessonTable.update { it.copy(timeTable = resolveTimeTable()) }
	}

	val _currentLessonTable = MutableStateFlow(LessonTableInfo(
		timeTable = TimeTable.Companion.DEFAULT,
		startDay = SettingModel.startDay,
		currentWeek = 0,
		totalWeek = SettingModel.totalWeek
	))
	val currentLessonTable: StateFlow<LessonTableInfo> = _currentLessonTable

	/** 当前星期 ( 0 - 6, 周一 —— 周日) */
	var _dayOfWeek = mutableIntStateOf(0)
	val dayOfWeek by _dayOfWeek

	/**
	 * 刷新当前课表，从数据库重新加载
	 */
	suspend fun refreshCurrentLessonTable(){
		_currentLessonTable.value = LessonTableInfo(
			timeTable = resolveTimeTable(),
			startDay = SettingModel.startDay,
			currentWeek = 0,
			totalWeek = SettingModel.totalWeek,
			lessons = getAllLesson()
		)
	}

	/**
	 * 保存新的课表
	 */
	suspend fun saveLessonTable(result: LessonTableQueryResult){
		val lessons = result.lessons ?: return

		val lessonChange = LessonUtils.mergeLesson(getAllLesson(), lessons)

		mergeLesson(lessonChange.first, lessonChange.second, lessonChange.third)

		_currentLessonTable.update { it.copy(
			startDay = result.startDay,
			totalWeek = result.totalWeek,
			lessons = getAllLesson()
		) }

		SettingModel.startDay = result.startDay
		SettingModel.totalWeek = result.totalWeek
	}

	fun updateStartDay(startDay: LocalDate){
		SettingModel.startDay = startDay
		_currentLessonTable.update { it.copy(startDay = startDay) }
	}

	fun updateTotalWeek(totalWeek: Int){
		SettingModel.totalWeek = totalWeek
		_currentLessonTable.update { it.copy(totalWeek = totalWeek) }
	}

	/**
	 * 向课表添加课程
	 */
	suspend fun appendLessonToLessonTable(lesson: Lesson): Boolean {
		val id = saveLesson(lesson)
		if(id != null && id != -1L){
			val newLesson = lesson.copy(id = id)
			_currentLessonTable.update { it.copy(
				lessons = it.lessons + newLesson
			) }
			return true
		}else{
			return false
		}
	}

	/**
	 * 更新课表中的课程
	 */
	suspend fun updateLessonTableLesson(lesson: Lesson): Boolean {
		if(updateLesson(lesson)){
			_currentLessonTable.update { it.copy(
				lessons = it.lessons.map { old -> if(lesson.id == old.id) old else old }
			) }
			return true
		}else{
			return false
		}
	}

	/**
	 * 删除课表中的课程
	 */
	suspend fun deleteLesson(lesson: Lesson): Boolean {
		val success = mergeLesson(emptyList(), emptyList(), listOf(lesson))
		if(success){
			_currentLessonTable.update { it.copy(
				lessons = it.lessons.filterNot { old -> old.id == lesson.id }
			) }
		}
		return success
	}

}