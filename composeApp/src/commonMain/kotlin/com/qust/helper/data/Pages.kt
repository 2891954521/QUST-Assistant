package com.qust.helper.data

import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.MyPage

object Pages {

    val Pages = mapOf(
        EmptyPage.key to EmptyPage,

        HomePage.key to HomePage,
    )

    val defaultPages: List<BasePage<*>> = listOf(EmptyPage, MyPage)

    operator fun get(key: String?) = Pages[key]
}



