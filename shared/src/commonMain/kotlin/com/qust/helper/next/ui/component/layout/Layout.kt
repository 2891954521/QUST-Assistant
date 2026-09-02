package com.qust.helper.next.ui.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qust.helper.next.ui.theme.color.ContainerColors


@Composable
fun SurfaceBox(
	modifier: Modifier = Modifier,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	content: @Composable BoxScope.() -> Unit
) {
	CompositionLocalProvider(LocalContentColor provides colors.content) {
		Box(modifier) {
			Box(modifier = Modifier.background(colors.background)) {
				content()
			}
		}
	}
}

@Composable
fun SurfaceColumn(
	modifier: Modifier = Modifier,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	content: @Composable ColumnScope.() -> Unit
) {
	CompositionLocalProvider(LocalContentColor provides colors.content) {
		Box(modifier) {
			Column(
				modifier = Modifier.fillMaxWidth().background(colors.background),
				verticalArrangement = verticalArrangement,
				horizontalAlignment = horizontalAlignment,
				content = content
			)
		}
	}
}

@Composable
fun SurfaceRow(
	modifier: Modifier = Modifier,
	colors: ContainerColors = ContainerColors.BlackOnWhite,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
	verticalAlignment: Alignment.Vertical = Alignment.Top,
	content: @Composable RowScope.() -> Unit
) {
	CompositionLocalProvider(LocalContentColor provides colors.content) {
		Box(modifier) {
			Row(modifier = Modifier.fillMaxWidth().background(colors.background), horizontalArrangement = horizontalArrangement, verticalAlignment = verticalAlignment, content = content)
		}
	}
}