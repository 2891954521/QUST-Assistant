package com.qust.helper.next

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.qust.helper.next.ui.component.LocalScale
import com.qust.helper.next.ui.component.Scale
import com.qust.helper.next.ui.component.Windows
import com.qust.helper.next.ui.router.BaseAppRouter
import com.qust.helper.next.ui.router.LocalRouter

object App {

    inline val windows: Windows
        @Composable @ReadOnlyComposable
        get() = Windows

    /**
     * 应用缩放
     */
    inline val scale: Scale
        @Composable @ReadOnlyComposable
        get() = LocalScale.current


    /**
     * 应用路由
     */
    inline val router: BaseAppRouter
        @Composable @ReadOnlyComposable
        get() = LocalRouter.current
}