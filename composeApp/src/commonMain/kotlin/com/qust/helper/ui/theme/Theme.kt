package com.qust.helper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColor = staticCompositionLocalOf { LightColors }

@Composable
fun AppTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		darkTheme -> LightColors.colorScheme
		else -> LightColors.colorScheme
	}
	CompositionLocalProvider(LocalColor provides LightColors) {
		MaterialTheme(
			colorScheme = colorScheme,
//			typography = Typography,
			content = content
		)
	}
}


