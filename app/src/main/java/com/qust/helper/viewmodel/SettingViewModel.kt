package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.account.EASAccount
import java.util.Date

class SettingViewModel(
	private val lessonTableViewModel: LessonTableViewModel
) : ViewModel() {

	var showAllLesson: Boolean = Setting.getBoolean(Keys.KEY_SHOW_ALL_LESSON, true)
		set(value) {
			Setting.edit { it.putBoolean(Keys.KEY_SHOW_ALL_LESSON, value) }
			lessonTableViewModel.showAllLesson.value = value
			field = value
		}

	var hideFinishLesson: Boolean = Setting.getBoolean(Keys.KEY_HIDE_FINISH_LESSON, false)
		set(value) {
			Setting.edit { it.putBoolean(Keys.KEY_HIDE_FINISH_LESSON, value) }
			lessonTableViewModel.hideFinishLesson.value = value
			field = value
		}

	var hideTeacher: Boolean = Setting.getBoolean(Keys.KEY_HIDE_TEACHER, false)
		set(value) {
			Setting.edit { it.putBoolean(Keys.KEY_HIDE_TEACHER, value) }
			lessonTableViewModel.hideTeacher.value = value
			field = value
		}

	var lockLesson: Boolean = Setting.getBoolean(Keys.KEY_LOCK_LESSON, false)
		set(value) {
			Setting.edit { it.putBoolean(Keys.KEY_LOCK_LESSON, value) }
//			lessonTableViewModel.
			field = value
		}

	var startDay: Date = lessonTableViewModel.lessonTable.value.startDay
		set(value) {
			lessonTableViewModel.lessonTable.value.startDay = value
			lessonTableViewModel.saveLessonTable()
			field = value
		}

	var totalWeek by mutableStateOf(lessonTableViewModel.lessonTable.value.totalWeek.toString())

	fun setTotalWeekValue(value: Int) {
		lessonTableViewModel.lessonTable.value.totalWeek = value
		lessonTableViewModel.saveLessonTable()
		totalWeek = value.toString()
	}

	var timeTable by mutableIntStateOf(lessonTableViewModel.currentTimeTable.intValue)
		private set

	fun setTimeTableValue(value: Int) {
		lessonTableViewModel.currentTimeTable.intValue = value
		Setting.edit { it.putInt(Keys.KEY_TIME_TABLE, value) }
		timeTable = value
	}

	var entranceTime by mutableStateOf(EASAccount.getInstance().entranceTime.toString())
		private set

	fun setEntranceTimeValue(value: Int) {
		EASAccount.getInstance().entranceTime = value
		entranceTime = value.toString()
	}

	var eaHost by mutableIntStateOf(Setting.getInt(Keys.EA_HOST, 0))
		private set

	fun setEaHostValue(value: Int){
		EASAccount.getInstance().changeHost(value)
		eaHost = value
	}

}