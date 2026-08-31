package com.qust.helper.next.ui.component.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.qust.helper.next.ui.component.ButtonType
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors
import io.github.composefluent.component.SubtleButton


object TextButtonColors {

	val Normal: ContainerColors
		@Composable
		@ReadOnlyComposable
		get() = ContainerColors(
			background = Theme.color.hover,
			content = Theme.color.textPrimary
		)

	val Secondary: ContainerColors
		@Composable
		@ReadOnlyComposable
		get() = ContainerColors(
			background = Theme.color.hover,
			content = Theme.color.textSecondary
		)

	val Danger: ContainerColors
		@Composable
		@ReadOnlyComposable
		get() = ContainerColors(
			background = Theme.color.errorContainer,
			content = Theme.color.error
		)
}



@Composable
fun TextButton(
	modifier: Modifier = Modifier,
	text: String,
	enabled: Boolean = true,
	type: ButtonType = ButtonType.Normal,
	colors: ContainerColors = TextButtonColors.Normal,
	onClick: () -> Unit
) {
	CompositionLocalProvider(
		LocalRippleConfiguration provides RippleConfiguration(color = colors.background),
	) {
		SubtleButton(modifier = modifier, onClick = onClick) {
			Text(
				text = text,
				style = type.textStyle,
				color = colors.content,
				modifier = Modifier.padding(type.innerPadding).alpha(if(enabled) 1F else 0.5F)
			)
		}
	}

}