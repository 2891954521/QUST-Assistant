package com.qust.helper.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.qust.helper.ui.widget.LessonTableView.LessonView
import com.qust.helper.viewmodel.LessonTableViewModel

object TermLesson {

	@Composable
	fun TermLesson(activity: ComponentActivity){
		val viewModel: LessonTableViewModel by activity.viewModels()
		LessonView(viewModel.lessonRender.value)
	}
}