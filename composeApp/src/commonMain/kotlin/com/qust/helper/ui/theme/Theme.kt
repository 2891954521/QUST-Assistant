package com.qust.helper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColor = staticCompositionLocalOf { LightColors }

val LocalTypography = staticCompositionLocalOf { Typography() }

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
		CompositionLocalProvider(LocalTypography provides createTypography()) {
			MaterialTheme(
				colorScheme = colorScheme,
				typography = LocalTypography.current,
				content = content
			)
		}
	}
}


