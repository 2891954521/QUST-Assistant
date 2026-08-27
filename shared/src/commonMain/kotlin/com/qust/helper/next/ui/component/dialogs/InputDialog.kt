package com.qust.helper.next.ui.component.dialogs

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.BaseDialog
import com.qust.helper.next.ui.component.PrimaryButton
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors


@Composable
fun InputDialog(
	visible: Boolean,
	title: String? = null,
	label: String? = null,
	content: String,
	onValueChange: (String) -> Unit,
	onDismiss: () -> Unit,
	onConfirm: () -> Unit,
	cancelText: String = "取消",
	okText: String = "确定",
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	maxLines: Int = 1,
	keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
	buttonContent: (@Composable RowScope.() -> Unit)? = null
){
	BaseDialog(
		visible = visible,
		onDismiss = onDismiss,
		colors = colors,
		buttonContent = {
			buttonContent?.invoke(this)
			PrimaryButton(modifier = Modifier.padding(24.dp), text = cancelText, onClick = onDismiss)
			PrimaryButton(modifier = Modifier.padding(24.dp), text = okText, onClick = onConfirm)
		}
	){
		if(title != null) Text(
			text = title,
			modifier = Modifier.padding(16.dp),
			style = Theme.textStyles.title,
			maxLines = 1
		)

		OutlinedTextField(
			value = content,
			onValueChange = onValueChange,
			modifier = Modifier.fillMaxWidth(),
			keyboardOptions = keyboardOptions,
			maxLines = maxLines,
			label = { if(label != null) Text(text = label) }
		)
	}
}