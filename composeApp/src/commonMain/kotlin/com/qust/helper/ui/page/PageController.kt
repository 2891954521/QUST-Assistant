package com.qust.helper.ui.page

import androidx.compose.runtime.Composable

interface PageController {

    /**
     * 打开一个新界面
     */
    fun startPage(key: String)

    fun startPage(page: BasePage<*>) = startPage(page.key)

    /**
     * 返回上一级
     */
    fun back()

}

@Composable
expect fun rememberPageController(): PageController