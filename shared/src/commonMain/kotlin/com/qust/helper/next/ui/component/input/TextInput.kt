package com.qust.helper.next.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.theme.Theme


@Composable
fun TextInput(
	modifier: Modifier = Modifier,
	value: String,
	onValueChange: (String) -> Unit,
	hint: String? = null,
	textStyle: TextStyle = Theme.textStyles.bodyMedium,
	keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
	content: (@Composable BoxScope.() -> Unit)? = null
) {
	BasicTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		keyboardOptions = keyboardOptions,
		textStyle = textStyle,
		maxLines = 1,
		cursorBrush = SolidColor(Color.Gray),
	) { innerTextField ->
		Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
			Box(modifier = Modifier.padding(8.dp)){
				if(hint != null && value.isEmpty()) Text(text = hint, style = textStyle)
				innerTextField()
			}
			content?.invoke(this)
		}
	}
}


@Composable
fun TextInput(
	modifier: Modifier = Modifier,
	value: String,
	onValueChange: (String) -> Unit,
	hint: String? = null,
	colors: ContainerColors,
	keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
	content: (@Composable BoxScope.() -> Unit)? = null
) {
	val textStyle = Theme.textStyles.bodyMedium.copy(color = colors.content)
	BasicTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		keyboardOptions = keyboardOptions,
		textStyle = textStyle,
		maxLines = 1,
		cursorBrush = SolidColor(Color.Gray),
	) { innerTextField ->
		Box(Modifier.wrapContentSize().fillMaxWidth().padding(6.dp).background(colors.background, RoundedCornerShape(6.dp)), contentAlignment = Alignment.CenterStart) {
			Box(modifier = Modifier.padding(8.dp)){
				if(hint != null && value.isEmpty()) Text(text = hint, style = textStyle)
				innerTextField()
			}
			content?.invoke(this)
		}
	}
}