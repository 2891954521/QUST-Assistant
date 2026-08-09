package com.qust.helper.next.ui.component

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

object Windows {

    /**
     * 屏幕像素大小
     */
    val LocalWindowsPixels = compositionLocalOf { IntSize(1920, 1080) }

    /**
     * 屏幕Dp大小
     */
    val LocalWindowsDpSize = compositionLocalOf { DpSize(1920.dp, 1080.dp) }

    /**
     * 窗口尺寸类型
     */
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    val LocalWindowsSize = compositionLocalOf { WindowSizeClass.calculateFromSize(DpSize(1920.dp, 1080.dp)) }

    /**
     * 屏幕像素大小
     */
    val windowsPixels: IntSize
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowsPixels.current

    /**
     * 屏幕Dp大小
     */
    val windowsDpSize: DpSize
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowsDpSize.current

    /**
     * 窗口尺寸类型
     */
    inline val windowsSize: WindowSizeClass
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowsSize.current

    /**
     * 是否是紧凑布局
     */
    inline val isCompact: Boolean
        @Composable
        @ReadOnlyComposable
        get() = windowsSize.widthSizeClass == WindowWidthSizeClass.Compact
}