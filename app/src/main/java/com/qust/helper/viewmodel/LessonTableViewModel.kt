package com.qust.helper.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.ui.widget.LessonRender
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
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

	var lessonTable = mutableStateOf(LessonTable(
		totalWeek = 20,
		lessons = Array(7){ arrayOfNulls(10) }
	))

	var lessonRender = mutableStateOf(LessonRender())

	init {
		loadLesson(application)
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
	private fun loadLesson(context: Context) {
		val dataFile = File(context.filesDir, "lessonTables")
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
		lessonRender.value.setLessonTable(lessonTable = lessonTable.value)
	}

}