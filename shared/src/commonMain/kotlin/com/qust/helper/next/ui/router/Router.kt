package com.qust.helper.next.ui.router

import androidx.compose.runtime.staticCompositionLocalOf
import com.qust.helper.next.ui.router.params.PageParamBuilder


expect val staticRouter: BaseAppRouter


expect fun getPageParamBuilder(): PageParamBuilder


/**
 * 页面控制器
 */
val LocalRouter = staticCompositionLocalOf { staticRouter }

