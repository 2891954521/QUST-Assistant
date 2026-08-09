package com.qust.helper.next.ui.router

import com.qust.helper.next.ui.router.params.PageParamBuilder

actual val staticRouter: BaseAppRouter = ActivityCollector

actual fun getPageParamBuilder(): PageParamBuilder = AndroidPageParamBuilder()
