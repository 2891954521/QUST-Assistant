package com.qust.helper.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.data.lesson.LessonTable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class LessonTableViewModel(application: Application): AndroidViewModel(application) {

	/**
	 * 当前时间表
	 */
	var currentTimeTable = mutableIntStateOf(Setting.getInt(Keys.KEY_TIME_TABLE, 0))

	/**
	 * 当前周 (从1开始)
	 */
	var currentWeek = mutableIntStateOf(0)

	/**
	 * 当前星期 ( 0-6, 周一 —— 周日)
	 */
	var dayOfWeek = mutableIntStateOf(0)

	/**
	 * 显示所有课程
	 */
	var showAllLesson = mutableStateOf(Setting.getBoolean(Keys.KEY_SHOW_ALL_LESSON, true))

	/**
	 * 隐藏已结课课程
	 */
	var hideFinishLesson =  mutableStateOf(Setting.getBoolean(Keys.KEY_HIDE_FINISH_LESSON, false))

	/**
	 * 隐藏教师
	 */
	var hideTeacher = mutableStateOf(Setting.getBoolean(Keys.KEY_HIDE_TEACHER, true))


	var lessonTable = mutableStateOf(LessonTable(
		totalWeek = 20,
		lessons = Array(7){ arrayOfNulls(10) }
	))

	private var dataFile: File

	init {
		dataFile = File(application.filesDir, "lessonTables")
		loadLesson()
		updateDate()
//		lessonTime = LessonTableViewModel.LESSON_TIME.get(currentTimeTable)
//		lessonTimeText = LessonTableViewModel.LESSON_TIME_TEXT.get(currentTimeTable)
	}

	/**
	 * 更新日期信息
	 */
	fun updateDate() {
		val currentDay: Calendar = Calendar.getInstance().also { it.firstDayOfWeek = Calendar.MONDAY }
		val startDay: Calendar = (currentDay.clone() as Calendar).also { it.time = lessonTable.value.startDay }

		dayOfWeek.intValue = currentDay[Calendar.DAY_OF_WEEK].let { if(it == Calendar.SUNDAY) 6 else it - 2 }
		currentWeek.intValue = (currentDay[Calendar.WEEK_OF_YEAR] - startDay[Calendar.WEEK_OF_YEAR] + 1).coerceAtLeast(1)
	}

	/**
	 * 从本地文件初始化课表
	 */
	@OptIn(ExperimentalSerializationApi::class)
	private fun loadLesson() {
		if(dataFile.exists()) {
			dataFile.listFiles()?.let{
				for(file in it){
					if(file?.exists() == true) {
						try{
							FileInputStream(file).use { stream ->
								lessonTable.value = Json.decodeFromStream<LessonTable>(stream)
							}
							break
						}catch(_: Exception) { }
					}
				}
			}
		} else {
			dataFile.mkdirs()
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun saveLessonTable(): Boolean{

		// 去除上课周数为0的课程
		val lessonGroups: Array<Array<LessonGroup?>> = lessonTable.value.lessons
		for(dailyLesson in lessonGroups) {
			for(timeSlot in dailyLesson.indices) {
				val group: LessonGroup = dailyLesson[timeSlot] ?: continue
				if(group.lessons.isEmpty()){
					dailyLesson[timeSlot] = null
					continue
				}
				group.lessons = group.lessons.filter { it.week != 0L }.toTypedArray()
			}
		}

		if(!dataFile.exists()) dataFile.mkdirs()
		val file = File(dataFile, "lessonTable")
		return try{
			FileOutputStream(file).use { stream ->
				Json.encodeToStream<LessonTable>(lessonTable.value, stream)
			}
			true
		}catch(_: Exception) {
			false
		}
	}

}