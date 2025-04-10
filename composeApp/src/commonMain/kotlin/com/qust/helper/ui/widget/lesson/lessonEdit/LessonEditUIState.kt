package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.qust.helper.model.lessonTable.LessonTableModel

class LessonEditUIState {

	val totalWeek by mutableStateOf(20)

	var lessonName by mutableStateOf("")
	var lessonPlace by mutableStateOf("")
	var lessonTeacher by mutableStateOf("")

	var colorIndex by mutableStateOf(0)

	val timeTable by LessonTableModel._timeTable

	var startHour = mutableStateOf("")
	var startMinute = mutableStateOf("")

	var endHour = mutableStateOf("")
	var endMinute = mutableStateOf("")

	val weeks = List(totalWeek){ false }.toMutableStateList()
}


interface LessonEditUIEvent {
	fun saveLesson()
	fun cancel()
}