package com.qust.helper.ui.page.eas

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.common.Texts
import com.qust.helper.ui.common.Texts.SingleLineText
import com.qust.helper.ui.common.ToastComponent
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.LessonRender
import com.qust.helper.ui.widget.LessonTableView
import com.qust.helper.ui.widget.ListPicker
import com.qust.helper.viewmodel.LessonTableViewModel
import com.qust.helper.viewmodel.eas.GetLessonTableViewModel

object GetLessonTable {

	@Composable
	fun GetLessonTable(activity: ComponentActivity) {
		val viewModel: GetLessonTableViewModel by activity.viewModels()
		val lessonTableViewModel: LessonTableViewModel by activity.viewModels()

		GetLessonTableUI(
			termText = viewModel.termText,
			termTimeText = viewModel.termTimeText,
			pickYear = viewModel.pickYear.value,
			pickType = viewModel.pickType.value,
			needSave = viewModel.needSave,
			lessonRender = viewModel.lessonRender.value,
			dialogText = viewModel.dialogText,
			toastContent = viewModel.toastContent,
			onYearPick = { viewModel.pickYear.value = it },
			onTypePick = { viewModel.pickType.value = it },
			doQuery = { viewModel.queryLesson(lessonTableViewModel) },
			saveLessonTable = { viewModel.saveLessonTable(lessonTableViewModel) }
		)
	}

	@Composable
	fun GetLessonTableUI(
		termText: String = "",
		termTimeText: String = "",
		pickYear: Int = 0,
		pickType: Int = 0,
		needSave: Boolean = false,
		lessonRender: LessonRender,
		dialogText: String = "",
		toastContent: MutableState<ToastContent>,
		onYearPick: (Int) -> Unit = { },
		onTypePick: (Int) -> Unit = { },
		doQuery: () -> Unit = { },
		saveLessonTable: () -> Unit = { }
	){
		var askForSave by remember { mutableStateOf(false) }

		Scaffold(
			floatingActionButton = {
				if(needSave){
					FloatingActionButton(onClick = { askForSave = true }) {
						Icon(Icons.Filled.Done, contentDescription = "")
					}
				}
			}
		) { padding ->

			Column(modifier = Modifier.padding(padding).fillMaxSize()) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically
				) {

					Text(text = stringResource(id = R.string.text_term))

					Spacer(Modifier.width(4.dp))

					ListPicker.NumberPicker(
						value = pickYear,
						range = Data.TermName.indices,
						label = { Data.TermName[it] },
						onValueChange = { onYearPick(it) },
						horizontalPadding = 8.dp
					)

					Spacer(Modifier.width(4.dp))

					ListPicker.NumberPicker(
						value = pickType,
						range = 0 .. 1,
						label = { if(it == 0) "个人课表" else "班级课表" },
						onValueChange = { onTypePick(it) },
						horizontalPadding = 8.dp
					)

					Button(modifier = Modifier.wrapContentSize().padding(8.dp), onClick = { doQuery() }) {
						Text(text = stringResource(id = R.string.text_query), maxLines = 1)
					}
				}

				SingleLineText(
					text = termText,
					color = colorSecondaryText,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)
				)

				LessonTableView.LessonView(lessonRender)
			}

			if(askForSave) {
				AskForSaveDialog(
					termTimeText = termTimeText,
					onDismiss = { askForSave = false },
					onConfirm = { askForSave = false; saveLessonTable() }
				)
			}

			AppBar.DialogBar(dialogText = dialogText)
			ToastComponent(toastContent)
		}
	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun AskForSaveDialog(
		termTimeText: String = "",
		onDismiss: () -> Unit = { },
		onConfirm: () -> Unit = { }
	){
		var updateStartTime by remember { mutableStateOf(true) }
		var keepUserLesson by remember { mutableStateOf(true) }

		ModalBottomSheet(onDismissRequest = { onDismiss() }) {
			Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
				Texts.SingleLineTextNoPadding(
					text = stringResource(id = R.string.text_save_lesson_table),
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.fillMaxWidth()
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Checkbox(checked = updateStartTime, onCheckedChange = { updateStartTime = it },)
					SingleLineText(text = "更新开学时间")
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
					SingleLineText(text = "保留被修改过的课程")
				}

				SingleLineText(
					text = "同一门课程若被用户修改过则会保留",
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
					modifier = Modifier.padding(start = 16.dp)
				)

				Row(modifier = Modifier.fillMaxWidth()) {
					TextButton(onClick = { onDismiss() }, modifier = Modifier.weight(1F)) {
						SingleLineText(text = stringResource(id = R.string.text_cancel), color = MaterialTheme.colorScheme.error)
					}
					TextButton(onClick = { onConfirm() }, modifier = Modifier.weight(1F)) {
						SingleLineText(text = stringResource(id = R.string.text_ok))
					}
				}
				
				Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
			}
		}
	}
}