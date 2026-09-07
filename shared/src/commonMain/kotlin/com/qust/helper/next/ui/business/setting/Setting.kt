package com.qust.helper.next.ui.business.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.ui.component.dialogs.InputDialog
import com.qust.helper.next.ui.component.spinner.Spinner
import com.qust.helper.next.ui.theme.Theme
import io.github.composefluent.component.Expander
import io.github.composefluent.component.ExpanderItem
import io.github.composefluent.component.Switcher


@Composable
fun SettingGroupUI(title: String, description: String? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
	val expanded = remember { mutableStateOf(false) }
	Expander(
		expanded = expanded.value,
		onExpandedChanged = { expanded.value = it },
		modifier = modifier,
		heading = { Text(title) },
		caption = { description?.let { CaptionText(it) } },
		expandContent = content
	)
}

@Composable
fun SettingItemUI(title: String, description: String, onClick: () -> Unit) {
	ExpanderItem(
		modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
		heading = { Text(title) },
		caption = { CaptionText(description) }
	)
}

@Composable
fun SwitchItemUI(
	title: String,
	description: String,
	enable: Boolean = true,
	onText: String = Strings.TEXT_ON,
	offText: String = Strings.TEXT_OFF,
	value: Boolean, 
	onChange: (Boolean) -> Unit
) {
	ExpanderItem(
		modifier = Modifier.fillMaxWidth(),
		heading = { Text(title) },
		caption = { CaptionText(description) },
		trailing = {
			Switcher(
				checked = value,
				onCheckStateChange = { onChange(it) },
				enabled = enable,
				textBefore = true,
				text = if (value) onText else offText
			)
		}
	)
}

@Composable
fun SpinnerItemUI(title: String, description: String, value: String, items: List<String>, onSelect: (Int, String) -> Unit) {
	ExpanderItem(
		modifier = Modifier.fillMaxWidth(),
		heading = { Text(title) },
		caption = { CaptionText(description) },
		trailing = {
			Spinner(
				content = value,
				data = items,
				onSelect = onSelect,
			)
		}
	)
}

@Composable
fun InputItemUI(title: String, label: String, value: String, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, onInput: (String) -> Unit) {
	var showInput by remember { mutableStateOf(false) }
	var inputValue by remember(value) { mutableStateOf(value) }

	ExpanderItem(
		modifier = Modifier.fillMaxWidth().clickable { showInput = true },
		heading = { Text(title) },
		caption = { CaptionText(value) }
	)

	InputDialog(
		visible = showInput,
		title = title,
		label = label,
		content = inputValue,
		keyboardOptions = keyboardOptions,
		onDismiss = { showInput = false },
		onValueChange = { inputValue = it },
		onConfirm = {
			onInput(inputValue)
			showInput = false
		}
	)
}

@Composable
fun ListItemUI(title: String, index: Int, items: Array<String>, enable: Boolean = true, onSelect: (Array<String>, Int) -> Unit) {
	var showList by remember { mutableStateOf(false) }
	Box(modifier = Modifier.fillMaxWidth().clickable { showList = true }){
		Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else Theme.color.onSurfaceVariant)
			Text(text = items[index], color = Theme.color.onSurfaceVariant)
		}
	}
	if(showList){
//		ListDialog(title = title, items, onDismiss = { showList = false }){ item, it ->
//			onSelect(item, it)
//			showList = false
//		}
	}
}


@Composable
private fun CaptionText(text: String){
	Text(text = text, modifier = Modifier.padding(end = 8.dp), color = Theme.color.textSecondary)
}
