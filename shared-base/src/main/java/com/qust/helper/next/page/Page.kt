package com.qust.helper.next.page

/**
 * 应用界面声明
 */
annotation class Page(

    /**
     * 页面跳转的key
     */
    val key: String,

    /**
     * Android环境是否为透明Activity
     */
    val androidTransparent: Boolean = false,

    /**
     * jvm环境是否在独立窗口显示page
     */
    val jvmSingleWindow: Boolean = false,

    /**
     * jvm环境是否始终让窗口置顶
     */
    val jvmOnTop: Boolean = false,
)