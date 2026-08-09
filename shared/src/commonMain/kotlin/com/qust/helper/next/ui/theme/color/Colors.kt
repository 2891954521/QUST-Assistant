package com.qust.helper.next.ui.theme.color

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalColor = staticCompositionLocalOf { AppColors() }

object Colors {

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
