package com.qust.helper.ui.page.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.LocalColor
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUI
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUI
import com.qust.helper.viewmodel.lesson.LessonTableViewModel
import kotlinx.coroutines.launch

object LessonTablePage: BasePage<LessonTableViewModel>("课表", Icons.Default.Home)  {

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
	@OptIn(ExperimentalMaterial3Api::class)
	fun LessonEditDialog(viewModel: LessonTableViewModel) {
		if(viewModel.isEditLesson){
			val scope = rememberCoroutineScope()
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			ModalBottomSheet(
				sheetState = sheetState,
				onDismissRequest = { viewModel.lessonEditUIEvent.cancel() },
				modifier = Modifier.padding(top = 16.dp),
				containerColor = LocalColor.current.surface
			) {
				LessonEditUI(
					uiState = viewModel.lessonEditUIState,
					uiEvent = viewModel.lessonEditUIEvent,
					onDismiss = { scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible){ viewModel.lessonEditUIEvent.cancel() } } },
				)
			}
		}
	}
}