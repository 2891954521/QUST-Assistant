package com.qust.helper.next.ui.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.composefluent.Typography


val LocalTypography: ProvidableCompositionLocal<AppTypography> = staticCompositionLocalOf { AppTypography.INSTANCE }


class AppTypography(
	val style: TextStyle = TextStyle(fontWeight = FontWeight.Normal)
) {
	companion object {
		val INSTANCE = AppTypography()
	}

	val caption = style.copy(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)

	val body = style.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)

	val bodyStrong = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)

	val bodyLarge = style.copy(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 24.sp)

	val subtitle = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp)

	val title = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp)

	val titleLarge = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 52.sp)

	val display = style.copy(fontWeight = FontWeight.SemiBold, fontSize = 68.sp, lineHeight = 92.sp)

	val typography = Typography(
		caption = caption,
		body = body,
		bodyStrong = bodyStrong,
		bodyLarge = bodyLarge,
		subtitle = subtitle,
		title = title,
		titleLarge = titleLarge,
		display = display
	)

	val mdTypography = androidx.compose.material3.Typography(
		displayLarge = titleLarge,
		displayMedium = title,
		displaySmall = subtitle,

		headlineLarge = bodyLarge,
		headlineMedium = body,
		headlineSmall = caption,

		titleLarge = titleLarge,
		titleMedium = title,
		titleSmall = subtitle,

		bodyLarge = body,
		bodyMedium = body,
		bodySmall = caption,

		labelLarge = bodyLarge,
		labelMedium = body,
		labelSmall = caption
	)
}