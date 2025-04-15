package com.qust.helper.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
actual fun Modifier.click(click: () -> Unit) = this.clickable(
	interactionSource = remember { MutableInteractionSource() },
	indication = ripple(),
	onClick = click
)