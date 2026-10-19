package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Android 端参考设计稿尺寸（dp）：
 * 1080 × 2400 px、420dpi 手机的 dp 尺寸
 */
private const val DESIGN_WIDTH_DP = 411F
private const val DESIGN_HEIGHT_DP = 914F

@Composable
actual fun LocalScaleProvider(uiScale: Float, fontScale: Float, content: @Composable (() -> Unit)) {
    // containerSize 是像素尺寸，旋转屏幕、分屏、折叠屏时都会变化
    val containerSize = LocalWindowInfo.current.containerSize

    val scale = remember(containerSize, uiScale, fontScale) {
        Scale(
            widthPixels = containerSize.width,
            heightPixels = containerSize.height,
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
