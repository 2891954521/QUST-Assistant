package com.qust.helper.ui.widget.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qust.helper.ui.theme.LocalColor
import com.qust.helper.ui.widget.InputDialog
import com.qust.helper.ui.widget.ListDialog
import com.qust.helper.ui.widget.click


@Composable
fun SettingGroupUI(title: String, content: @Composable () -> Unit) {
	Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)) {
		Text(
			text = title,
			modifier = Modifier.padding(start = 16.dp, top = 16.dp),
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.primary
		)
		content()
	}
}

@Composable
fun SettingItemUI(title: String, description: String, enable: Boolean = true, onClick: () -> Unit) {
	Box(modifier = Modifier.fillMaxWidth().click(onClick)) {
		Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else LocalColor.current.onSurfaceVariant)
			if(description.isNotEmpty()) Text(text = description, color = LocalColor.current.onSurfaceVariant)
		}
	}
}

@Composable
fun SwitchItemUI(title: String, onText: String, offText: String? = null, value: Boolean, enable: Boolean = true, onChange: (Boolean) -> Unit) {
	Box(modifier = Modifier.fillMaxWidth().click {  }) {
		Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Column(modifier = Modifier.weight(1F)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else LocalColor.current.onSurfaceVariant)
				Text(text = if(value) onText else offText ?: onText, color = LocalColor.current.onSurfaceVariant)
			}
			Switch(
				modifier = Modifier.semantics { contentDescription = title }.padding(start = 8.dp),
				checked = value,
				enabled = enable,
				onCheckedChange = { onChange(it) }
			)
		}
	}
}

@Composable
fun InputItemUI(title: String, value: String, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, enable: Boolean = true, onInput: (String) -> Unit) {
	var showInput by remember { mutableStateOf(false) }
	Box(modifier = Modifier.fillMaxWidth().click { showInput = true }){
		Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else LocalColor.current.onSurfaceVariant)
			Text(text = value, color = LocalColor.current.onSurfaceVariant)
		}
	}
	if(showInput){
		InputDialog(title = title, content = value, keyboardOptions = keyboardOptions, onDismiss = { showInput = false }){
			onInput(it)
			showInput = false
		}
	}
}

@Composable
fun ListItemUI(title: String, index: Int, items: Array<String>, enable: Boolean = true, onSelect: (Array<String>, Int) -> Unit) {
	var showList by remember { mutableStateOf(false) }
	Box(modifier = Modifier.fillMaxWidth().click { showList = true }){
		Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
			Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else LocalColor.current.onSurfaceVariant)
			Text(text = items[index], color = LocalColor.current.onSurfaceVariant)
		}
	}
	if(showList){
		ListDialog(title = title, items, onDismiss = { showList = false }){ item, it ->
			onSelect(item, it)
			showList = false
		}
	}
}
