package com.qust.helper.data

import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.MyPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.lesson.LessonTablePage

object Pages {

    val Pages = mapOf(
        EmptyPage.key to EmptyPage,

        HomePage.key to HomePage,

        LessonTablePage.key to LessonTablePage,

        SettingPage.key to SettingPage
    )

    val defaultPages: List<BasePage<*>> = listOf(LessonTablePage, MyPage)

    operator fun get(key: String?) = Pages[key]
}



