package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.data.i18n.Strings
import com.qust.helper.ui.theme.LESSON_BACKGROUND_COLORS
import com.qust.helper.ui.theme.LocalColor

@Composable
fun LessonEditUI(uiState: LessonEditUIState, uiEvent: LessonEditUIEvent, onDismiss: () -> Unit){
	val scrollState = rememberScrollState()
	Column(modifier = Modifier.fillMaxSize()) {

		Box(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
			TextButton(modifier = Modifier.clip(CircleShape).align(Alignment.CenterStart), onClick = onDismiss) {
				Text(text = "取消", color = MaterialTheme.colorScheme.error)
			}
			Text(
				text = "编辑课程",
				modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Center),
				style = MaterialTheme.typography.titleLarge,
			)
			TextButton(modifier = Modifier.clip(CircleShape).align(Alignment.CenterEnd), onClick = {
				uiEvent.saveLesson()
			}) {
				Text(text = "保存")
			}
		}

		Column(
			modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(scrollState),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {

			EditItem("名称", uiState.lessonName){ uiState.lessonName = it }
			EditItem("教室", uiState.lessonPlace){ uiState.lessonPlace = it }
			EditItem("教师", uiState.lessonTeacher){ uiState.lessonTeacher = it }

			LessonWeekPicker("上课时间", uiState.week, Strings.ARRAY_WEEK_NAME)
			LessonTimePicker("开始时间", uiState.startHour, uiState.startMinute, uiState.timeTable.startTimeStr)
			LessonTimePicker("结束时间", uiState.endHour, uiState.endMinute, uiState.timeTable.endTimeStr)

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Text(text = "上课周数", modifier = Modifier.weight(1F))
				TextButton(onClick = { for(i in 0 until uiState.totalWeek) uiState.weeks[i] = true }) {
					Text(text = "全选")
					0 .. 10
				}
				TextButton(onClick = { for(i in 0 until uiState.totalWeek) uiState.weeks[i] = i % 2 == 0 }) {
					Text(text = "单周")
				}
				TextButton(onClick = { for(i in 0 until uiState.totalWeek) uiState.weeks[i] = i % 2 == 1 }) {
					Text(text = "双周")
				}
			}

			LessonWeeksPicker(counts = uiState.totalWeek, rows = 6, uiState.weeks)

			Text(text = "课程颜色")

			LessonColorPicker(colors = LESSON_BACKGROUND_COLORS, select = uiState.colorIndex, rows = 6, onClick = { uiState.colorIndex = it })
		}
	}
}

@Composable
private fun EditItem(
	title: String,
	value: String,
	onValueChange: (String) -> Unit,
){
	Row(
		Modifier.fillMaxWidth().border(1.dp, LocalColor.current.primary, RoundedCornerShape(6.dp)).padding(8.dp),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {

		Text(title)

		BasicTextField(
			value = value,
			onValueChange = onValueChange,
			modifier = Modifier.fillMaxWidth().padding(8.dp),
			textStyle = TextStyle.Default.copy(textAlign = TextAlign.End),
			maxLines = 1,
		)
	}
}