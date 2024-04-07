package com.qust.helper.ui.page

import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.qust.helper.ui.activity.BaseActivity
import com.qust.helper.ui.page.lesson.TermLesson
import com.qust.helper.viewmodel.TermLessonViewModel

object HomePage {

	@Composable
	fun HomePage(activity: BaseActivity, padding: PaddingValues){
		val viewModel by activity.viewModels<TermLessonViewModel>()
		TermLesson.TermLessonUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent, toast = activity.toast)
	}
}