package com.qust.helper.next.ui.theme.color

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalColor = staticCompositionLocalOf { AppColors() }

object Colors {

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

    val LESSON_BACKGROUND_COLOR_SECOND = Color(0xC0F5F5F5)
    val LESSON_TEXT_COLOR_SECOND = Color(0xFF909090)

}

open class AppColors {

    val textPrimary = Color(0xFF080808)
    val textSecondary = Color(0xFF666666)

    val textPrimaryVariant = Color(0xFFCDCDCD)
    val textSecondaryVariant = Color(0xFF999999)

    val primary = Color(0xFF0E60A9)
    val onPrimary = Color(0xFFFFFFFF)

    val primaryContainer = Color(0xFFD4E3FF)
    val onPrimaryContainer = Color(0xFF001C39)

    val secondary = Color(0xFF006491)
    val onSecondary = Color(0xFFFFFFFF)

    val secondaryContainer = Color(0xFFC9E6FF)
    val onSecondaryContainer = Color(0xFF001E2F)

    val tertiary = Color(0xFF0E60A9)
    val onTertiary = Color(0xFFFFFFFF)
    val tertiaryContainer = Color(0xFF01579B)
    val onTertiaryContainer = Color(0xFFD4E3FF)

    val background = Color(0xFFF5F5F5)
    val onBackground = Color(0xFF1A1C1E)

    val surface = Color(0xFFF5F5F5)
    val onSurface = Color(0xFF212121)
    val surfaceVariant = Color(0xFFFFFFFF)
    val onSurfaceVariant = Color(0xFF43474E)

    val error = Color(0xFFBA1A1A)
    val onError = Color(0xFFFFFFFF)

    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF410002)

    val outline = Color(0xFF73777F)
    val outlineVariant = Color(0xFFC3C6CF)

    val hover = Color(0x09000000)

    val colorScheme = ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = Color.Blue,

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

        surfaceTint = Color.Blue,

        inverseSurface = Color.DarkGray,
        inverseOnSurface = Color.White,

        error = error,
        onError = onError,

        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,

        outline = outline,
        outlineVariant = outlineVariant,

        scrim = Color.Black,

        surfaceBright = Color.Unspecified,
        surfaceDim = Color.Unspecified,

        surfaceContainer = Color.Unspecified,

        surfaceContainerHigh = Color.Unspecified,
        surfaceContainerHighest = Color.Unspecified,

        surfaceContainerLow = Color.Unspecified,
        surfaceContainerLowest = Color.Unspecified,

        primaryFixed = Color.Unspecified,
        primaryFixedDim = Color.Unspecified,
        onPrimaryFixed = Color.Unspecified,
        onPrimaryFixedVariant = Color.Unspecified,

        secondaryFixed = Color.Unspecified,
        secondaryFixedDim = Color.Unspecified,
        onSecondaryFixed = Color.Unspecified,
        onSecondaryFixedVariant = Color.Unspecified,

        tertiaryFixed = Color.Unspecified,
        tertiaryFixedDim = Color.Unspecified,
        onTertiaryFixed = Color.Unspecified,
        onTertiaryFixedVariant = Color.Unspecified,
    )

}
