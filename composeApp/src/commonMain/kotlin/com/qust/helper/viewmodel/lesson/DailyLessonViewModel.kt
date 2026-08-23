package com.qust.helper.viewmodel.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

/**
 * 当日课表 ViewModel
 */
class DailyLessonViewModel : BaseViewModel() {

	var lessons by mutableStateOf(emptyList<Lesson>())
	var dayOfWeekText by mutableStateOf("")
	var currentWeek by mutableStateOf(0)
	var timeTable by mutableStateOf(TimeTable.DEFAULT)

	init {
		runBackGround {
			LessonTableRepository.currentLessonTable.collectLatest { info ->
				val today = DateUtils.today()
				val week = today.dayOfWeek.isoDayNumber - 1
				val currentW = if(info.startDay != LocalDate(1, 1, 1)) {
					DateUtils.calcWeekOffset(info.startDay, today).coerceAtLeast(0)
				} else {
					info.currentWeek
				}
				val dayLessons = info.lessons.filter { it.week == week && (it.weeks and (1L shl currentW)) > 0 }
					.sortedBy { it.startMinute }
				lessons = dayLessons
				timeTable = info.timeTable
				currentWeek = currentW
				dayOfWeekText = when(week) {
					0 -> "周一"; 1 -> "周二"; 2 -> "周三"; 3 -> "周四"
					4 -> "周五"; 5 -> "周六"; 6 -> "周日"
					else -> ""
				}
			}
		}
	}

	fun refresh() {
		runBackGround {
			LessonTableRepository.refreshCurrentLessonTable()
		}
	}
}
