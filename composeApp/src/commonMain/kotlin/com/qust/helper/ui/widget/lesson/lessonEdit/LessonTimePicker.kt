package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.ui.theme.LocalColor


@Composable
fun LessonTimePicker(
	title: String,
	hour: MutableState<String>,
	minute: MutableState<String>,

	items: List<String>,
) {
	var showSpinner by remember { mutableStateOf(false) }

	PickerUI(
		title = title,
		hour = hour.value,
		minute = minute.value,

		items = items,
		showSpinner = showSpinner,

		onOpen = { showSpinner = true },
		onDismiss = { showSpinner = false },
		onHourChange = {
			if(it.length <= 2) hour.value = it
		},
		onMinuteChange = {
			if(it.length <= 2) minute.value = it
		},
		onSelect = { i, it ->
			showSpinner = false
			val sp = it.split(":")
			if(sp.size == 2){
				hour.value = sp[0]
				minute.value = sp[1]
			}
		}
	)
}


@Composable
fun PickerUI(
	title: String = "",
	hour: String,
	minute: String,
	items: List<String>,
	showSpinner: Boolean,
	onOpen: () -> Unit = { },
	onDismiss: () -> Unit = { },
	onHourChange: (String) -> Unit = { },
	onMinuteChange: (String) -> Unit = { },
	onSelect: (Int, String) -> Unit = { _, _ -> }
) {
	val density = LocalDensity.current
	var spinnerWidth by remember { mutableStateOf(100.dp) }

	Box(Modifier.fillMaxWidth().border(1.dp, LocalColor.current.primary, RoundedCornerShape(6.dp))) {
		Row (modifier = Modifier.height(IntrinsicSize.Min).onGloballyPositioned { coordinates ->
			with(density) { spinnerWidth = coordinates.size.width.toDp() }
		}.padding(8.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(text = title)

			Spacer(Modifier.weight(2F))

			BasicTextField(
				value = hour,
				onValueChange = onHourChange,
				modifier = Modifier.weight(1F).padding(8.dp).width(50.dp),
				textStyle = TextStyle.Default.copy(textAlign = TextAlign.End),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)

			Text(text = " : ")

			BasicTextField(
				value = minute,
				onValueChange = onMinuteChange,
				modifier = Modifier.weight(1F).padding(8.dp).width(50.dp),
				textStyle = TextStyle.Default.copy(textAlign = TextAlign.Start),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)

			Box(modifier = Modifier.fillMaxHeight().clickable(onClick = onOpen)) {
				Icon(Icons.Rounded.KeyboardArrowDown, null, modifier = Modifier.align(Alignment.Center).size(18.dp), tint = LocalColor.current.primary)
			}
		}
		DropdownMenu(modifier = Modifier.background(LocalColor.current.surface), expanded = showSpinner, onDismissRequest = onDismiss) {
			items.forEachIndexed { index, item ->
				Box(modifier = Modifier.width(spinnerWidth).fillMaxWidth().clickable { onSelect(index, item) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
					Text(text = "${index + 1}", modifier = Modifier.align(Alignment.CenterStart))
					Text(text = item, modifier = Modifier.align(Alignment.CenterEnd))
				}
			}
		}
	}
}