package com.qust.helper.next.ui.component.spinner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.theme.Theme
import io.github.composefluent.component.DropDownButton
import io.github.composefluent.component.FlyoutContainerScope
import io.github.composefluent.component.FlyoutPlacement
import io.github.composefluent.component.MenuFlyoutContainer
import io.github.composefluent.component.MenuFlyoutItem


@Preview
@Composable
private fun SpinnerPreview() {
	var content by remember { mutableStateOf("default") }

	SpinnerWidget(
		modifier = Modifier.size(100.dp),
		data = listOf("1", "2", "3"),
		onSelect = { i, it -> content = it },
	){
		Text(content, modifier = Modifier.fillMaxWidth().clickable { isFlyoutVisible = true })
	}

}


@Composable
fun Spinner(
	modifier: Modifier = Modifier,
	content: String,
	data: List<String>,
	onSelect: (Int, String) -> Unit,
	textStyle: TextStyle = Theme.textStyles.body,
	placement: FlyoutPlacement = FlyoutPlacement.BottomAlignedStart
){
	SpinnerWidget(
		data = data,
		modifier = modifier,
		onSelect = onSelect,
		textStyle = textStyle,
		placement = placement,
	){
		DropDownButton(
			onClick = { isFlyoutVisible = !isFlyoutVisible },
			contentArrangement = Arrangement.SpaceBetween,
			content = {
				Text(text = content, modifier = Modifier.padding(8.dp))
			}
		)
	}
}


@Composable
fun SpinnerWidget(
	modifier: Modifier = Modifier,
	data: List<String>,
	onSelect: (Int, String) -> Unit,
	textStyle: TextStyle = Theme.textStyles.body,
	placement: FlyoutPlacement = FlyoutPlacement.Auto,
	content: @Composable FlyoutContainerScope.() -> Unit
) {
	MenuFlyoutContainer(
		flyout = {
			data.forEachIndexed { index, item ->
				MenuFlyoutItem(text = {
					Text(text = item, style = textStyle, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
				}, onClick = {
					isFlyoutVisible = false
					onSelect(index, item)
				})
			}
		},
		modifier = modifier,
		content = content,
		adaptivePlacement = true,
		placement = placement
	)
}