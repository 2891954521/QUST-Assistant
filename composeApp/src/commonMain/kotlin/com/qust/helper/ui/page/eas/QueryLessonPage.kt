package com.qust.helper.ui.page.eas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUI
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.ui.widget.picker.ListItemPicker
import com.qust.helper.viewmodel.eas.QueryLessonViewModel


object QueryLessonTable: BasePage<QueryLessonViewModel>("课表查询", Drawables.School) {

	@Composable
	override fun getViewModel() = viewModel<QueryLessonViewModel>()

	@Composable
	override fun Content(viewModel: QueryLessonViewModel) {
		var askForSave by remember { mutableStateOf(false) }

		Scaffold(
			floatingActionButton = {
				if(viewModel.needSave){
					FloatingActionButton(onClick = { askForSave = true }) {
						Icon(Icons.Filled.Done, contentDescription = "")
					}
				}
			}
		) { _ ->
			BaseEasQueryUI(
				pickYear = viewModel.pickYear.value,
				onYearPick = { viewModel.pickYear.value = it },
				doQuery = { viewModel.queryLesson() },
				searchBar = {
					SearchBar(
						pickType = viewModel.pickType.intValue,
						onTypePick = { viewModel.pickType.intValue = it },
					)
				},
			){
				GetLessonTableUI(
					termText = viewModel.termText,
					lessonTable = viewModel.lessonUIState,
				)
			}
		}

		if(askForSave) {
			AskForSaveDialog(
				termTimeText = viewModel.termTimeText,
				onDismiss = { askForSave = false },
				onConfirm = { askForSave = false; viewModel.saveLesson() }
			)
		}
	}


	@Composable
	private fun GetLessonTableUI(
		termText: String = "",
		lessonTable: LessonTableUIState,
	){

		Text(
			text = termText,
			color = colorSecondaryText,
			style = MaterialTheme.typography.bodySmall,
			modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)
		)

		LessonTableUI(lessonTable)
	}


	@Composable
	private fun SearchBar(
		pickType: Int,
		onTypePick: (Int) -> Unit
	){
		ListItemPicker(
			value = Strings.ARRAY_QUERY_LESSON_TYPE[pickType],
			list = Strings.ARRAY_QUERY_LESSON_TYPE,
			onValueChange = { i, _ -> onTypePick(i) },
			horizontalPadding = 8.dp
		)
	}


	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	private fun AskForSaveDialog(
		termTimeText: String = "",
		onDismiss: () -> Unit = { },
		onConfirm: () -> Unit = { }
	){
		var updateStartTime by remember { mutableStateOf(true) }
		var keepUserLesson by remember { mutableStateOf(true) }

		ModalBottomSheet(onDismissRequest = { onDismiss() }) {
			Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
				Text(
					text = Strings.TEXT_SAVE_LESSON_TABLE,
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.fillMaxWidth()
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Checkbox(checked = updateStartTime, onCheckedChange = { updateStartTime = it },)
					Text(text = "更新开学时间")
				}

				Text(
					text = termTimeText,
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
					modifier = Modifier.padding(start = 16.dp)
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Checkbox(checked = keepUserLesson, onCheckedChange = { keepUserLesson = it },)
					Text(text = "保留被修改过的课程")
				}

				Text(
					text = "同一门课程若被用户修改过则会保留",
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
					modifier = Modifier.padding(start = 16.dp)
				)

				Row(modifier = Modifier.fillMaxWidth()) {
					TextButton(onClick = { onDismiss() }, modifier = Modifier.weight(1F)) {
						Text(text = Strings.TEXT_CANCEL, color = MaterialTheme.colorScheme.error)
					}
					TextButton(onClick = { onConfirm() }, modifier = Modifier.weight(1F)) {
						Text(text = Strings.TEXT_OK)
					}
				}

				Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
			}
		}
	}
}