package com.qust.helper.next.ui.business.lesson.lessonEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.ui.component.button.TextButton
import com.qust.helper.next.ui.component.input.TextInput
import com.qust.helper.next.ui.component.spinner.Spinner
import com.qust.helper.next.ui.component.spinner.SpinnerWidget
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.Colors.LESSON_BACKGROUND_COLORS
import io.github.composefluent.component.DropDownButton
import io.github.composefluent.component.SplitButton

@Composable
fun LessonEditUI(uiState: LessonEditUIState, uiEvent: LessonEditUIEvent, onDismiss: () -> Unit){
	val lessonTableInfo by uiState.lessonTableInfo.collectAsStateWithLifecycle()

	Column(modifier = Modifier.fillMaxSize()) {

		Box(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
			TextButton(text = "取消", modifier = Modifier.align(Alignment.CenterStart), onClick = onDismiss)

			Text(text = "编辑课程", modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Center), style = Theme.textStyles.bodyLarge)

			TextButton(text = "保存", modifier = Modifier.align(Alignment.CenterEnd), onClick = { uiEvent.saveLesson() })
		}

		Column(
			modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(text = "名称")
			TextInput(value = uiState.lessonName, onValueChange = { uiState.lessonName = it }, modifier = Modifier.fillMaxWidth(), maxLines = 1)

			Text(text = "教室")
			TextInput(value = uiState.lessonPlace, onValueChange = { uiState.lessonPlace = it }, modifier = Modifier.fillMaxWidth(), maxLines = 1)

			Text(text = "教师")
			TextInput(value = uiState.lessonTeacher, onValueChange = { uiState.lessonTeacher = it }, modifier = Modifier.fillMaxWidth(), maxLines = 1)

			Text(text = "上课时间")
			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				Spinner(
					content = Strings.ARRAY_WEEK_NAME[uiState.week.value],
					data = Strings.ARRAY_WEEK_NAME,
					onSelect = { i, it -> uiState.week.value = i },
				)

				TimePick(hour = uiState.startHour, minute = uiState.startMinute, items = lessonTableInfo.timeTable.startTimeStr)

				TimePick(hour = uiState.endHour, minute = uiState.endMinute, items = lessonTableInfo.timeTable.endTimeStr)
			}

			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(text = "上课周数", modifier = Modifier.weight(1F))
				TextButton(text = "全选", onClick = { for(i in 0 until lessonTableInfo.totalWeek) uiState.weeks[i] = true })
				TextButton(text = "单周", onClick = { for(i in 0 until lessonTableInfo.totalWeek) uiState.weeks[i] = i % 2 == 0 })
				TextButton(text = "双周", onClick = { for(i in 0 until lessonTableInfo.totalWeek) uiState.weeks[i] = i % 2 == 1 })
			}

			LessonWeeksPicker(counts = lessonTableInfo.totalWeek, itemWidth = 64.dp, uiState.weeks)

			Text(text = "课程颜色")

			LessonColorPicker(colors = LESSON_BACKGROUND_COLORS, select = uiState.colorIndex, itemWidth = 64.dp, onClick = { uiState.colorIndex = it })
		}
	}
}

@Composable
private fun TimePick(
	modifier: Modifier = Modifier,
	hour: MutableState<String>,
	minute: MutableState<String>,
	items: List<String>,
){
	SpinnerWidget(
		data = items,
		modifier = modifier,
		onSelect = { i, it ->
			val sp = it.split(":")
			if(sp.size == 2){
				hour.value = sp[0]
				minute.value = sp[1]
			}
	    },
	){
		SplitButton(flyoutClick = { isFlyoutVisible = true }, onClick = { }) {
			BasicTextField(
				value = hour.value,
				onValueChange = {
					hour.value = when(it.length){
						0 -> ""
						1 -> if(!it.startsWith('0')) "0$it" else it
						else -> it.substring(it.length - 2, it.length)
					}
				},
				modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp).width(20.dp),
				textStyle = Theme.textStyles.body.copy(textAlign = TextAlign.End),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)

			Text(text = ":")

			BasicTextField(
				value = minute.value,
				onValueChange = {
					minute.value = when(it.length){
						0 -> ""
						1 -> if(!it.startsWith('0')) "0$it" else it
						else -> it.substring(it.length - 2, it.length)
					}
				},
				modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp).width(20.dp),
				textStyle = Theme.textStyles.body.copy(textAlign = TextAlign.Start),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)
		}
	}
}