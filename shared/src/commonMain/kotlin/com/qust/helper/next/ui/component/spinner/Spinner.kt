package com.qust.helper.next.ui.component.spinner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.next.App
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.theme.Theme


@Preview
@Composable
private fun SpinnerPreview() {
	var content by remember { mutableStateOf("default") }
	var showSpinner by remember { mutableStateOf(false) }

	SpinnerWidget(
		modifier = Modifier.size(100.dp),
		show = showSpinner,
		data = listOf("1", "2", "3"),
		onSelect = { i, it -> content = it },
		onDismiss = { showSpinner = false }
	){
		Text(content, modifier = Modifier.fillMaxWidth().clickable { showSpinner = true })
	}

}

@Composable
fun SpinnerWidget(
	modifier: Modifier = Modifier,
	show: Boolean,
	data: List<String>,
	onSelect: (Int, String) -> Unit,
	onDismiss: () -> Unit,
	textStyle: TextStyle = Theme.textStyles.bodyMedium,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	content: @Composable BoxScope.() -> Unit
) {
	val scale = App.scale
	var spinnerWidth by remember { mutableStateOf(100.dp) }

	Box(modifier = modifier) {
		Box(modifier = Modifier.onGloballyPositioned { coordinates ->
			spinnerWidth = Dp(coordinates.size.width * scale.uiReScale)
		}, content = content)
		SpinnerPop(data, show, spinnerWidth, textStyle, colors, onDismiss, onSelect)
	}
}

@Composable
fun SpinnerPop(
	data: List<String>,
	showSpinner: Boolean,
	spinnerWidth: Dp,
	textStyle: TextStyle,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	onDismiss: () -> Unit,
	onSelect: (Int, String) -> Unit
) {
	DropdownMenu(expanded = showSpinner, onDismissRequest = onDismiss, modifier = Modifier.background(colors.background)) {
		CompositionLocalProvider(LocalDensity provides App.scale.getDensity()) {
			data.forEachIndexed { index, item ->
				Box(modifier = Modifier.width(spinnerWidth).fillMaxWidth().clickable { onSelect(index, item) }) {
					Text(text = item, style = textStyle, color = colors.content, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
				}
			}
		}
	}
}