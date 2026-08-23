package com.qust.helper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.qust.helper.model.SettingModel

val LocalColor = staticCompositionLocalOf<AppColors> { LightColors }

val LocalTypography = staticCompositionLocalOf { Typography() }

@Composable
fun AppTheme(
	darkTheme: Boolean = SettingModel.themeFollowSystem.value && isSystemInDarkTheme() || (!SettingModel.themeFollowSystem.value && SettingModel.themeDark.value),
	content: @Composable () -> Unit
) {
	val colors = if(darkTheme) DarkColors else LightColors
	CompositionLocalProvider(LocalColor provides colors) {
		CompositionLocalProvider(LocalTypography provides createTypography()) {
			MaterialTheme(
				colorScheme = colors.colorScheme,
				typography = LocalTypography.current,
				content = content
			)
		}
	}
}
