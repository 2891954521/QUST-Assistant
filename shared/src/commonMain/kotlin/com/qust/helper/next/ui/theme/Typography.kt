package com.qust.helper.next.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


val LocalTypography: ProvidableCompositionLocal<AppTypography> = staticCompositionLocalOf { AppTypography.INSTANCE }


class AppTypography(
	val style: TextStyle = TextStyle(
		fontWeight = FontWeight.Normal
	)
) {
	companion object {
		val INSTANCE = AppTypography()
	}

	/** 最大的标题，通常用于中等强调且长度较短的文本。衬线体或无衬线字体在字幕中表现良好。 */
	val titleLarge = style.copy(fontSize = 32.sp)

	/** 第二大标题，通常用于中等强调且长度较短的文本。衬线体或无衬线字体在字幕中表现良好。 */
	val titleMedium = style.copy(fontSize = 30.sp, fontWeight = FontWeight.Medium)

	/** 最小的标题，通常用于中等强调且长度较短的文本。衬线体或无衬线字体在字幕中表现良好。 */
	val titleSmall = style.copy(fontSize = 28.sp, fontWeight = FontWeight.Medium)

	/** 最大的正体，通常用于长篇写作，因为它适合较小的文本尺寸。对于较长的文本段落，建议使用衬线体或无衬线字体。 */
	val bodyLarge = style.copy(fontSize = 28.sp)

	/** 第二大正体，通常用于长文写作，因为它适合小文本大小。对于较长的文本段落，建议使用衬线体或无衬线字体。 */
	val bodyMedium = style.copy(fontSize = 25.sp)

	/** 最小的正体，通常用于长文写作，因为它适用于小文本大小。对于较长的文本段落，建议使用衬线体或无衬线字体。 */
	val bodySmall = style.copy(fontSize = 22.sp)

	/** 标签大文字是一种行动号召，用于不同类型的按钮（如文本、带框和包含的按钮）以及标签页、对话框和卡片中。按钮文字通常为无衬线字体。 */
	val labelLarge = style.copy(fontSize = 32.sp, fontWeight = FontWeight.Medium)

	/** 最小的字体大小之一。它被用来少量用于注释图像或介绍标题 */
	val labelMedium = style.copy(fontSize = 28.sp, fontWeight = FontWeight.Medium)

	/** 最小的字体大小之一。它被用来少量用于注释图像或介绍标题 */
	val labelSmall = style.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium)

	val typography = Typography(

		displayLarge = titleLarge,
		displayMedium = titleMedium,
		displaySmall = titleSmall,

		headlineLarge = bodyLarge,
		headlineMedium = bodyMedium,
		headlineSmall = bodySmall,

		titleLarge = titleLarge,
		titleMedium = titleMedium,
		titleSmall = titleSmall,

		bodyLarge = bodyLarge,
		bodyMedium = bodyMedium,
		bodySmall = bodySmall,

		labelLarge = labelLarge,
		labelMedium = labelMedium,
		labelSmall = labelSmall
	)

}