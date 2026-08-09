package com.qust.helper.next.ui.router

import com.qust.helper.next.ui.router.params.MapPageParamBuilder
import com.qust.helper.next.ui.router.params.PageParamBuilder

actual val staticRouter: BaseAppRouter = DesktopPageController

actual fun getPageParamBuilder(): PageParamBuilder = MapPageParamBuilder()
