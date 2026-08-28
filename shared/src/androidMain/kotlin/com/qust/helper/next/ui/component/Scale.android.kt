package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

@Composable
actual fun LocalScaleProvider(uiScale: Float, fontScale: Float, content: @Composable (() -> Unit)) {
    val window = LocalWindowInfo.current
    val orientation = LocalConfiguration.current.orientation

    val scale = remember {
        Scale(
            widthPixels = window.containerSize.width,
            heightPixels = window.containerSize.height,
            uiScale = uiScale,
            fontScale = fontScale,
            orientation = UiOrientation.from(orientation)
        )
    }

    CompositionLocalProvider(
        LocalScale provides scale,
        LocalDensity provides scale.getDensity(),
        content = content
    )
}