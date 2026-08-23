package com.qust.helper.ui.page.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.GridView
import com.qust.helper.ui.page.BackHandler
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.widget.dialog.BottomDialog
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUI
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUI
import com.qust.helper.viewmodel.lesson.LessonPopupType
import com.qust.helper.viewmodel.lesson.LessonTableViewModel

object LessonTablePage: BasePage<LessonTableViewModel>("学期课表", Drawables.GridView)  {

	@Composable
	override fun getViewModel() = viewModel<LessonTableViewModel>()

	@Composable
	override fun Content(viewModel: LessonTableViewModel) {
		BackHandler(viewModel.isEditLesson) {
			viewModel.isEditLesson = false
		}

		Box(Modifier.fillMaxSize()) {

			LessonTableUI(
				uiState = viewModel.tableUIState,
				onLessonClick = { i, it -> viewModel.clickLesson(i, it) },
				onLessonLongClick = { i, it, offset -> viewModel.longPressLesson(i, it, offset) },
				onBlankLongClick = { week, timeSlot, offset -> viewModel.longPressBlank(week, timeSlot, offset) },
			)

			if(viewModel.isShowPopup){
				LessonPopMenu(viewModel)
			}

			Button(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), onClick = {
				viewModel.clickLesson(-1, null)
			}){
				Text("添加课程")
			}
		}

		LessonEditDialog(viewModel)
	}

	@Composable
	fun LessonPopMenu(viewModel: LessonTableViewModel) {
		Popup(offset = viewModel.popupPosition, onDismissRequest = { viewModel.dismissPopup() }) {
			Card(
				shape = RoundedCornerShape(8.dp),
				elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
			) {
				Row {
					if(viewModel.popupType == LessonPopupType.LESSON){
						MenuText("编辑"){ viewModel.editLesson() }
						MenuText("复制"){ viewModel.copyLesson() }
						MenuText("删除"){ viewModel.deleteLessonAt() }
					}else{
						if(viewModel.hasCopyLesson){
							MenuText("粘贴"){ viewModel.pasteLesson() }
						}
						MenuText("添加新课"){ viewModel.addLesson() }
					}
				}
			}
		}
	}

	@Composable
	fun MenuText(text: String, onClick: () -> Unit){
		Box(modifier = Modifier.clickable(onClick = onClick)){
			Text(text = text, modifier = Modifier.padding(12.dp))
		}
	}

	@Composable
	fun LessonEditDialog(viewModel: LessonTableViewModel) {
		BottomDialog(isExpanded = viewModel.isEditLesson) {
			LessonEditUI(
				uiState = viewModel.editUIState,
				uiEvent = viewModel,
				onDismiss = { viewModel.cancel() },
			)
		}
	}
}