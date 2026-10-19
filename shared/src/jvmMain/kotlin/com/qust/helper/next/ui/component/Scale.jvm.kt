package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * 桌面端参考设计稿尺寸（dp）
 */
private const val DESIGN_WIDTH_DP = 1024F
private const val DESIGN_HEIGHT_DP = 640F

@Composable
actual fun LocalScaleProvider(uiScale: Float, fontScale: Float, content: @Composable (() -> Unit)) {
    val pixels = Windows.windowsPixels

    val scale = remember(pixels, uiScale, fontScale) {
        Scale(
            widthPixels = pixels.width,
            heightPixels = pixels.height,
            designWidth = DESIGN_WIDTH_DP,
            designHeight = DESIGN_HEIGHT_DP,
            uiScale = uiScale,
            fontScale = fontScale
        )
    }

    CompositionLocalProvider(
        LocalScale provides scale,
        LocalDensity provides scale.getDensity(),
        content = content
    )
}
