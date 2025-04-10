package com.qust.helper.ui.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.widget.lessonTable.LessonTableUI
import com.qust.helper.viewmodel.LessonTableViewModel

object LessonTable: BasePage<LessonTableViewModel>("课表", Icons.Default.Home)  {

	@Composable
	override fun getViewModel() = viewModel<LessonTableViewModel>()

	@Composable
	override fun Content(viewModel: LessonTableViewModel) {
		LessonTableUI(viewModel.lessonTableUIState)
	}
}