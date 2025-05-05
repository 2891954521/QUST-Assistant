package com.qust.helper.data

import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.MyPage
import com.qust.helper.ui.page.account.AccountManagerPage
import com.qust.helper.ui.page.account.EasLoginPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.eas.QueryAcademicPage
import com.qust.helper.ui.page.eas.QueryExamPage
import com.qust.helper.ui.page.eas.QueryLessonPage
import com.qust.helper.ui.page.eas.QueryMarkPage
import com.qust.helper.ui.page.eas.QueryNoticePage
import com.qust.helper.ui.page.lesson.LessonTablePage

object Pages {

    val Pages = mapOf(
        EmptyPage.key to EmptyPage,

        HomePage.key to HomePage,

        LessonTablePage.key to LessonTablePage,

        AccountManagerPage.key to AccountManagerPage,
        EasLoginPage.key to EasLoginPage,

        QueryLessonPage.key to QueryLessonPage,
        QueryMarkPage.key to QueryMarkPage,
        QueryExamPage.key to QueryExamPage,
        QueryAcademicPage.key to QueryAcademicPage,
        QueryNoticePage.key to QueryNoticePage,

        SettingPage.key to SettingPage
    )

    val defaultPages: List<BasePage<*>> = listOf(LessonTablePage, MyPage)

    operator fun get(key: String?) = Pages[key]
}



