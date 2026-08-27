package com.qust.helper.next.ui.component.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors

object ButtonStyle {

	val TextButtonDefault: ContainerColors
		@Composable
		@ReadOnlyComposable
		get() = ContainerColors(
			background = Theme.color.hover,
			content = Theme.color.textPrimary
		)

}