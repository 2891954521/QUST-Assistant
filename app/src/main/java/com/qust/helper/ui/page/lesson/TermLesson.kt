package com.qust.helper.ui.page.lesson

import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.ui.theme.LESSON_BACKGROUND_COLORS
import com.qust.helper.ui.widget.LessonTableView.LessonView
import com.qust.helper.ui.widget.Picker
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.TermLessonUIEvent
import com.qust.helper.viewmodel.TermLessonUIState
import com.qust.helper.viewmodel.TermLessonViewModel
import kotlinx.coroutines.launch

object TermLesson {

	val TermLessonPage = Page(Keys.Page.TermLessonPage, "学期课表", iconRes = R.drawable.ic_grid_view) { activity, padding, _ ->
		val viewModel by activity.viewModels<TermLessonViewModel>()
		TermLessonUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent, toast = activity.toast)
	}

	@Composable
	fun TermLessonUI(padding: PaddingValues, uiState: TermLessonUIState, uiEvent: TermLessonUIEvent, toast: Toast){
		var popupLocation by remember { mutableStateOf(Pair(0, 0)) }

		Box(modifier = Modifier.padding(padding)){
			LessonView(uiState.lessonRender, { uiEvent.clickLesson(it) }, { selectLesson, week, x, y ->
				popupLocation = Pair(x, y)
				uiEvent.longClickLesson(selectLesson, week)
			})
		}

		if(uiState.isShowPopup){
			PopMenu(uiState, uiEvent, popupLocation, onDismiss = { uiState.isShowPopup = false })
		}

		toast.ToastContent(uiState.toastContent)

		EditLessonDialog(uiState = uiState, uiEvent = uiEvent)
	}

	@Composable
	fun PopMenu(uiState: TermLessonUIState, uiEvent: TermLessonUIEvent, pair: Pair<Int, Int>, onDismiss: () -> Unit = { }){
		Popup(offset = IntOffset(pair.first, pair.second), onDismissRequest = { onDismiss() }) {
			Card(shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
				Row {
					Box(modifier = Modifier.clickable { uiEvent.editLesson() }){
						Text(text = "编辑", modifier = Modifier.padding(12.dp))
					}
					if(uiState.copyEnable){
						Box(modifier = Modifier.clickable { uiEvent.copyLesson() }){
							Text(text = "复制", modifier = Modifier.padding(12.dp))
						}
					}
					if(uiState.pasteEnable){
						Box(modifier = Modifier.clickable { uiEvent.pasteLesson() }){
							Text(text = "粘贴", modifier = Modifier.padding(12.dp))
						}
					}
					if(uiState.deleteEnable){
						Box(modifier = Modifier.clickable { uiEvent.deleteLesson() }){
							Text(text = "删除", modifier = Modifier.padding(12.dp))
						}
					}
					Box(modifier = Modifier.clickable { uiEvent.addLesson() }){
						Text(text = "添加新课", modifier = Modifier.padding(12.dp))
					}
				}

			}
		}
	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun EditLessonDialog(uiState: TermLessonUIState, uiEvent: TermLessonUIEvent) {
		if(uiState.isShowEdit){
			val scope = rememberCoroutineScope()
			val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			ModalBottomSheet(sheetState = sheetState, onDismissRequest = { uiEvent.hideEdit() }, modifier = Modifier.padding(top = 16.dp)) {
				EditLessonUI(
					uiState = uiState,
					uiEvent = uiEvent,
					onDismiss = { scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible){ uiEvent.hideEdit() } } },
				)
			}
		}
	}

	@Composable
	fun EditLessonUI(uiState: TermLessonUIState, uiEvent: TermLessonUIEvent, onDismiss: () -> Unit = { }){
		val scrollState = rememberScrollState()
		Column(modifier = Modifier.fillMaxSize()) {

			Box(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
				TextButton(modifier = Modifier.clip(CircleShape).align(Alignment.CenterStart), onClick = { onDismiss() }) {
					Text(text = "取消", color = MaterialTheme.colorScheme.error)
				}
				Text(
					text = "编辑课程",
					modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Center),
					style = MaterialTheme.typography.titleLarge,
				)
				TextButton(modifier = Modifier.clip(CircleShape).align(Alignment.CenterEnd), onClick = {
					if(uiState.name.isEmpty()){
						uiEvent.toastWarning("请输入课程名称")
						return@TextButton
					}
					uiEvent.saveLesson()
				}) {
					Text(text = "保存")
				}
			}

			Column(
				modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(scrollState),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {

				OutlinedTextField(
					value = uiState.name,
					onValueChange = { uiState.name = it },
					label = { Text(text = "课程名称") },
					isError = uiState.name.isEmpty(),
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = uiState.place,
					onValueChange = { uiState.place = it },
					label = { Text(text = "教室") },
					modifier = Modifier.fillMaxWidth()
				)

				OutlinedTextField(
					value = uiState.teacher,
					onValueChange = { uiState.teacher = it },
					label = { Text(text = "教师") },
					modifier = Modifier.fillMaxWidth()
				)

				Box(modifier = Modifier.fillMaxWidth()) {
					Text(text = "上课节数", modifier = Modifier.align(Alignment.CenterStart))
					Picker.AnimNumberPicker(
						modifier = Modifier.align(Alignment.CenterEnd),
						number = uiState.len,
						onAdd = { if(uiState.len < 10) uiState.len++ },
						onSub = { if(uiState.len > 1) uiState.len-- }
					)
				}

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(text = "上课周数", modifier = Modifier.weight(1F))
					TextButton(onClick = { for(i in 0 ..< uiState.totalWeek) uiState. week[i] = true }) {
						Text(text = "全选")
						0 .. 10
					}
					TextButton(onClick = { for(i in 0 ..< uiState.totalWeek) uiState.week[i] = i % 2 == 0 }) {
						Text(text = "单周")
					}
					TextButton(onClick = { for(i in 0 ..< uiState.totalWeek) uiState.week[i] = i % 2 == 1 }) {
						Text(text = "双周")
					}
				}

				Picker.DragAblePicker(counts = uiState.totalWeek, rows = 6, uiState.week)

				Text(text = "课程颜色")

				Picker.ColorPicker(colors = LESSON_BACKGROUND_COLORS, select = uiState.color, rows = 6, onClick = { uiState.color = it })
			}
		}
	}
}