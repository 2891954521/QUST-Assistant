package com.qust.helper.ui.page.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.GridView
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.widget.dialog.BottomDialog
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUI
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUI
import com.qust.helper.viewmodel.lesson.LessonTableViewModel

object LessonTablePage: BasePage<LessonTableViewModel>("学期课表", Drawables.GridView)  {

	@Composable
	override fun getViewModel() = viewModel<LessonTableViewModel>()

	@Composable
	override fun Content(viewModel: LessonTableViewModel) {
		Box(Modifier.fillMaxSize()) {

			LessonTableUI(viewModel.lessonTableUIState)

			Button(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), onClick = {
				viewModel.isEditLesson = true
			}){
				Text("添加课程")
			}
		}

		LessonEditDialog(viewModel)
	}


	@Composable
	fun LessonEditDialog(viewModel: LessonTableViewModel) {
		BottomDialog(isExpanded = viewModel.isEditLesson) {
			LessonEditUI(
				uiState = viewModel.lessonEditUIState,
				uiEvent = viewModel.lessonEditUIEvent,
				onDismiss = { viewModel.lessonEditUIEvent.cancel() },
			)
		}
	}
}