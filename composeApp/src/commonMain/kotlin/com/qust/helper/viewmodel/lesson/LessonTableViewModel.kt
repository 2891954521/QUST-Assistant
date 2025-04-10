package com.qust.helper.viewmodel.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIEvent
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIState
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.viewmodel.BaseViewModel

open class LessonTableViewModel : BaseViewModel() {

	val lessonTableUIState = LessonTableUIState()

	val lessonEditUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	val lessonEditUIEvent = object : LessonEditUIEvent {
		override fun saveLesson() {
		}

		override fun cancel() {
			isEditLesson = false
		}

	}

}