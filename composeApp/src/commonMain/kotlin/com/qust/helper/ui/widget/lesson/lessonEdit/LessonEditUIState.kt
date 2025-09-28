package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.qust.helper.repository.LessonTableRepository

class LessonEditUIState {

	val lessonTableInfo = LessonTableRepository.currentLessonTable

	var lessonName by mutableStateOf("")
	var lessonPlace by mutableStateOf("")
	var lessonTeacher by mutableStateOf("")

	var colorIndex by mutableStateOf(0)

	val week = mutableStateOf(0)

	val startHour = mutableStateOf("")
	val startMinute = mutableStateOf("")

	val endHour = mutableStateOf("")
	val endMinute = mutableStateOf("")

	val weeks = List(lessonTableInfo.value.totalWeek){ false }.toMutableStateList()
}


interface LessonEditUIEvent {
	fun saveLesson()
	fun cancel()
}