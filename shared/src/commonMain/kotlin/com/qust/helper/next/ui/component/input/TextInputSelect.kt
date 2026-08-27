package com.qust.helper.next.ui.component.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.spinner.SpinnerWidget
import com.qust.helper.next.ui.theme.color.ContainerColors


@Preview
@Composable
private fun EditableSpinnerPreview() {
	var content by remember { mutableStateOf("") }
	InputSelectableWidget(
		value = content,
		data = listOf("1", "2", "3"),
		onInput = { content = it },
		onSelect = { i, it -> content = it }
	)
}


@Composable
fun InputSelectableWidget(
	modifier: Modifier = Modifier,
	value: String,
	hint: String? = null,
	data: List<String>,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
	onInput: (String) -> Unit,
	onSelect: (Int, String) -> Unit
) {
	SpinnerWidget(
		modifier = modifier,
		data = data,
		onSelect = onSelect,
	){
		TextInput(
			modifier = Modifier.fillMaxWidth(),
			value = value,
			onValueChange = onInput,
			hint = hint,
			colors = colors,
 			keyboardOptions = keyboardOptions
		){
			Box(modifier = Modifier.align(Alignment.CenterEnd).clickable { isFlyoutVisible = true }) {
				Icon(Icons.Default.ArrowDropDown, null, Modifier.padding(8.dp), tint = colors.content)
			}
		}
	}
}