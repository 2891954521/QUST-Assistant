package com.qust.helper.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color


object LightColors: AppColors(
	primary = Color(0xFF0E60A9),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFD4E3FF),
	onPrimaryContainer = Color(0xFF001C39),
	inversePrimary = Color(0xFFA4C9FF),

	secondary = Color(0xFF006491),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFC9E6FF),
	onSecondaryContainer = Color(0xFF001E2F),

	tertiary = Color(0xFF0E60A9),
	onTertiary = Color(0xFFFFFFFF),
	tertiaryContainer = Color(0xFF01579B),
	onTertiaryContainer = Color(0xFFD4E3FF),

	background = Color(0xFFF5F5F5),
	onBackground = Color(0xFF1A1C1E),

	surface = Color(0xFFF5F5F5),
	onSurface = Color(0xFF212121),
	surfaceVariant = Color(0xFFFFFFFF),
	onSurfaceVariant = Color(0xFF43474E),
	surfaceTint = Color(0xFF0E60A9),
	inverseSurface = Color(0xFF2F3033),
	inverseOnSurface = Color(0xFFF1F0F4),

	error = Color(0xFFBA1A1A),
	onError = Color(0xFFFFFFFF),

	errorContainer = Color(0xFFFFDAD6),
	onErrorContainer = Color(0xFF410002),

	outline = Color(0xFF73777F),
	outlineVariant = Color(0xFFC3C6CF),

	scrim = Color(0xFF000000),
)

open class AppColors(
	val primary: Color,
	val onPrimary: Color,
	val primaryContainer: Color,
	val onPrimaryContainer: Color,
	val secondary: Color,
	val onSecondary: Color,
	val secondaryContainer: Color,
	val onSecondaryContainer: Color,
	val tertiary: Color,
	val onTertiary: Color,
	val tertiaryContainer: Color,
	val onTertiaryContainer: Color,
	val error: Color,
	val errorContainer: Color,
	val onError: Color,
	val onErrorContainer: Color,
	val background: Color,
	val onBackground: Color,
	val surface: Color,
	val onSurface: Color,
	val surfaceVariant: Color,
	val onSurfaceVariant: Color,
	val outline: Color,
	val inverseOnSurface: Color,
	val inverseSurface: Color,
	val inversePrimary: Color,
	val surfaceTint: Color,
	val outlineVariant: Color,
	val scrim: Color,

	val toastBackground: Color = Color(0x9A000000),
	val toastIconColor: Color = Color(0xFFFFFFFF),
	val toastTextColor: Color = Color(0xFFCDCDCD),
){
	val colorScheme = ColorScheme(
		primary = primary,
		onPrimary = onPrimary,
		primaryContainer = primaryContainer,
		onPrimaryContainer = onPrimaryContainer,
		inversePrimary = inversePrimary,
		secondary = secondary,
		onSecondary = onSecondary,
		secondaryContainer = secondaryContainer,
		onSecondaryContainer = onSecondaryContainer,
		tertiary = tertiary,
		onTertiary = onTertiary,
		tertiaryContainer = tertiaryContainer,
		onTertiaryContainer = onTertiaryContainer,
		background = background,
		onBackground = onBackground,
		surface = surface,
		onSurface = onSurface,
		surfaceVariant = surfaceVariant,
		onSurfaceVariant = onSurfaceVariant,
		surfaceTint = surfaceTint,
		inverseSurface = inverseSurface,
		inverseOnSurface = inverseOnSurface,
		error = error,
		errorContainer = errorContainer,
		onError = onError,
		onErrorContainer = onErrorContainer,
		outline = outline,
		outlineVariant = outlineVariant,
		scrim = scrim,
	)
}


val colorSecondaryText = Color(0xFF757575)

val colorSuccess = Color(0xFF1DE9B6)
val colorError = Color(0xFFCF6679)

val icons = Color(0xFF212121)
val divider = Color(0xFFBDBDBD)

val LESSON_BACKGROUND_COLORS = arrayOf(
	Color(0xFFE6F4FF),
	Color(0xFFFDEBDD),
	Color(0xFFDEFBF7),
	Color(0xFFEEEDFF),
	Color(0xFFFCEBCD),
	Color(0xFFFFEFF0),
	Color(0xFFEAF2FF),
	Color(0xFFFFEEF8),
	Color(0xFFE2F9F3),
	Color(0xFFFFF9C9),
	Color(0xFFFAEDFF),
	Color(0xFFF4F2FD),
)

val LESSON_TEXT_COLORS = arrayOf(
	Color(0xFF1F9DD0),
	Color(0xFFDA7762),
	Color(0xFF54AEB9),
	Color(0xFF817CCC),
	Color(0xFFE39B2D),
	Color(0xFFD8637D),

	Color(0xFF6A8ED8),
	Color(0xFFDD73B3),
	Color(0xFF44AF9F),
	Color(0xFFC5A723),
	Color(0xFFAF70CB),
	Color(0xFF768AC5),
)

val TEXT_COLOR_SECOND_COLOR = Color(0xFF909090)