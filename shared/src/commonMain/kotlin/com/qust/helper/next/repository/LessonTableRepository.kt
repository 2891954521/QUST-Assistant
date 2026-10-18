package com.qust.helper.next.repository

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.SettingKeys
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.LessonTableData
import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.entity.lesson.TimeTableStoreCodec
import com.qust.helper.next.module.database.AppDataBase
import com.qust.helper.next.module.database.dao.LessonDao
import com.qust.helper.next.module.database.dao.toLesson
import com.qust.helper.next.module.database.dao.toLessonDao
import com.qust.helper.next.module.lesson.LessonUtils
import com.qust.helper.next.network.entity.lesson.LessonTableQueryResult
import com.qust.helper.next.repository.LessonTableRepository.lessonTimeTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

object LessonTableRepository {

	val _currentLessonTable = MutableStateFlow(LessonTableData(
			timeTable = TimeTableRepository.currentTimeTable.value,
			startDay = SettingRepository.startDay,
			totalWeek = SettingRepository.totalWeek
		)
	)
	val currentLessonTable: StateFlow<LessonTableData> = _currentLessonTable

	/**
	 * 已保存课程所对应的时间表，用于「按新时间表对齐课程」
	 *
	 * 切换时间表不会改变它；只有教务导入与显式对齐会更新。
	 */
	var lessonTimeTable: TimeTable = TimeTableStoreCodec.decodeTimeTable(
			AppSetting.getString(SettingKeys.SETTING_LESSON_TIME_TABLE, "")
		) ?: TimeTableRepository.currentTimeTable.value
		private set

	/** 当前星期 ( 0 - 6, 周一 —— 周日) */
	var _dayOfWeek = mutableIntStateOf(0)
	val dayOfWeek by _dayOfWeek

	/**
	 * 课表显示设置
	 */
	val setting: StateFlow<LessonTableSetting> field = MutableStateFlow(LessonTableSetting())

	/**
	 * 刷新当前课表，从数据库重新加载
	 */
	suspend fun refreshCurrentLessonTable(){
		_currentLessonTable.value = LessonTableData(
			timeTable = TimeTableRepository.currentTimeTable.value,
			startDay = SettingRepository.startDay,
			totalWeek = SettingRepository.totalWeek,
			lessons = getAllLesson()
		)
	}

	/**
	 * 应用新的时间表，由 [TimeTableRepository] 在配置变更后调用
	 */
	fun applyTimeTable(timeTable: TimeTable) {
		_currentLessonTable.update { it.copy(timeTable = timeTable) }
	}

	private fun setLessonTimeTable(timeTable: TimeTable) {
		lessonTimeTable = timeTable
		AppSetting[SettingKeys.SETTING_LESSON_TIME_TABLE] = TimeTableStoreCodec.encodeTimeTable(timeTable)
	}

	/**
	 * 按节次把已保存课程对齐到当前生效的时间表
	 *
	 * 起点为 [lessonTimeTable]（上次导入/对齐使用的时间表），终点为当前时间表；
	 * 早于起点第一节、或终点缺少对应节次的课程保持不动。
	 *
	 * @return 实际更新的课程数
	 */
	suspend fun alignLessonsToTimeTable(): Int {
		val to = _currentLessonTable.value.timeTable
		val from = lessonTimeTable
		if(from == to) return 0

		val aligned = getAllLesson().mapNotNull { lesson ->
			val mapped = from.mapLessonMinutes(lesson.startMinute, lesson.endMinute, to) ?: return@mapNotNull null
			if(mapped.first == lesson.startMinute && mapped.second == lesson.endMinute) {
				null
			}else {
				lesson.copy(startMinute = mapped.first, endMinute = mapped.second)
			}
		}
		if(aligned.isEmpty()) return 0

		val updated = try {
			AppDataBase.INSTANCE.lessonDao().updateAll(aligned.map(Lesson::toLessonDao))
		}catch(e: Exception) {
			Logger.e("alignLessonsToTimeTable", e)
			return 0
		}

		setLessonTimeTable(to)
		if(updated > 0) refreshCurrentLessonTable()
		return updated
	}

	/**
	 * 保存新的课表
	 */
	suspend fun saveLessonTable(result: LessonTableQueryResult){
		val lessons = result.lessons ?: return

		val lessonChange = LessonUtils.mergeLesson(getAllLesson(), lessons)

		mergeLesson(lessonChange.first, lessonChange.second, lessonChange.third)

		// 导入的课程是按当前时间表映射的，记录来源以便之后按新时间表对齐
		setLessonTimeTable(_currentLessonTable.value.timeTable)

		_currentLessonTable.update { it.copy(
			startDay = result.startDay,
			totalWeek = result.totalWeek,
			lessons = getAllLesson()
		) }

		SettingRepository.startDay = result.startDay
		SettingRepository.totalWeek = result.totalWeek
	}

	fun updateStartDay(startDay: LocalDate){
		SettingRepository.startDay = startDay
		_currentLessonTable.update { it.copy(startDay = startDay) }
	}

	fun updateTotalWeek(totalWeek: Int){
		SettingRepository.totalWeek = totalWeek
		_currentLessonTable.update { it.copy(totalWeek = totalWeek) }
	}

	fun updateSetting(setting: LessonTableSetting){
		this.setting.value = setting
		setting.save()
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


	private suspend fun getAllLesson(): List<Lesson> {
		return AppDataBase.INSTANCE.lessonDao().selectAll().map(LessonDao::toLesson)
	}

	private suspend fun saveLesson(lesson: Lesson): Long? {
		return AppDataBase.INSTANCE.lessonDao().insert(lesson.toLessonDao())
	}

	private suspend fun updateLesson(lesson: Lesson): Boolean {
		return AppDataBase.INSTANCE.lessonDao().update(lesson.toLessonDao()) == 1
	}

	private suspend fun mergeLesson(new: List<Lesson>, update: List<Lesson>, delete: List<Lesson>): Boolean {
		try{
			AppDataBase.INSTANCE.lessonDao().mergeLesson(new.map(Lesson::toLessonDao), update.map(Lesson::toLessonDao), delete.map(Lesson::toLessonDao))
			return true
		}catch(e: Exception){
			return false
		}
	}

	@Immutable
	data class LessonTableSetting(
		val showNonCurrentWeek: Boolean = AppSetting[SettingKeys.SETTING_LESSON_SHOW_NON_CURRENT_WEEK, false],
		val showFinished: Boolean = AppSetting[SettingKeys.SETTING_LESSON_SHOW_FINISHED, false],
		val hideTeacher: Boolean = AppSetting[SettingKeys.SETTING_LESSON_HIDE_TEACHER, false],
		val showWeekend: Boolean = true
	){
		fun save(){
			AppSetting[SettingKeys.SETTING_LESSON_SHOW_NON_CURRENT_WEEK] = showNonCurrentWeek
			AppSetting[SettingKeys.SETTING_LESSON_SHOW_FINISHED] = showFinished
			AppSetting[SettingKeys.SETTING_LESSON_HIDE_TEACHER] = hideTeacher
		}
	}
}