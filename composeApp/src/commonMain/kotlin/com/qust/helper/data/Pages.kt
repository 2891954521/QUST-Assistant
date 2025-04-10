package com.qust.helper.data

import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.LessonTable
import com.qust.helper.ui.page.MyPage

object Pages {

    val Pages = mapOf(
        EmptyPage.key to EmptyPage,

        HomePage.key to HomePage,

        LessonTable.key to LessonTable
    )

    val defaultPages: List<BasePage<*>> = listOf(LessonTable, MyPage)

    operator fun get(key: String?) = Pages[key]
}



