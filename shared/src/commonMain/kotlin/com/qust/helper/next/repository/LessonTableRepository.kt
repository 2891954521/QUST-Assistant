package com.qust.helper.next.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.next.entity.lesson.LessonTableInfo
import com.qust.helper.next.module.database.AppDataBase
import com.qust.helper.next.module.database.dao.LessonDao
import com.qust.helper.next.module.database.dao.toLesson
import com.qust.helper.next.module.database.dao.toLessonDao
import com.qust.helper.next.module.lesson.LessonUtils
import com.qust.helper.next.network.entity.lesson.LessonTableQueryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

object LessonTableRepository {

	val _currentLessonTable = MutableStateFlow(LessonTableInfo(
			timeTable = TimeTable.DEFAULT,
			startDay = SettingRepository.startDay,
			currentWeek = 0,
			totalWeek = SettingRepository.totalWeek
		)
	)
	val currentLessonTable: StateFlow<LessonTableInfo> = _currentLessonTable

	/** 当前星期 ( 0 - 6, 周一 —— 周日) */
	var _dayOfWeek = mutableIntStateOf(0)
	val dayOfWeek by _dayOfWeek

	/**
	 * 刷新当前课表，从数据库重新加载
	 */
	suspend fun refreshCurrentLessonTable(){
		_currentLessonTable.value = LessonTableInfo(
			timeTable = TimeTable.DEFAULT,
			startDay = SettingRepository.startDay,
			currentWeek = 0,
			totalWeek = SettingRepository.totalWeek,
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
}