package com.qust.helper.next.ui.component

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

@Composable
actual fun LocalScaleProvider(content: @Composable (() -> Unit)) {
    val pixels = Windows.windowsPixels
    val windowSize = Windows.windowsSize

    val scale = remember(pixels, windowSize) {
        Scale(
            widthPixels = pixels.width,
            heightPixels = pixels.height,
            uiScale = AppUIScale,
            fontScale = AppFontScale,
            orientation = if(windowSize.widthSizeClass == WindowWidthSizeClass.Compact) UiOrientation.PORTRAIT else UiOrientation.LANDSCAPE
        )
    }

    CompositionLocalProvider(
        LocalScale provides scale,
        LocalDensity provides scale.getDensity(),
        content = content
    )
}