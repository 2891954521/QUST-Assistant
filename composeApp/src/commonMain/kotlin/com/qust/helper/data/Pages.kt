package com.qust.helper.data

import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.MyPage
import com.qust.helper.ui.page.account.AccountManagerPage
import com.qust.helper.ui.page.account.EasLoginPage
import com.qust.helper.ui.page.account.IpassLoginPage
import com.qust.helper.ui.page.app.AppPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.app.UpdatePage
import com.qust.helper.ui.page.business.ElectricRechargePage
import com.qust.helper.ui.page.business.SportTestPage
import com.qust.helper.ui.page.eas.QueryAcademicPage
import com.qust.helper.ui.page.eas.QueryExamPage
import com.qust.helper.ui.page.eas.QueryLessonPage
import com.qust.helper.ui.page.eas.QueryMarkPage
import com.qust.helper.ui.page.eas.QueryNoticePage
import com.qust.helper.ui.page.lesson.DailyLessonPage
import com.qust.helper.ui.page.lesson.LessonTablePage
import com.qust.helper.ui.page.third.DrinkPage
import com.qust.helper.ui.page.web.WebPage

object Pages {

    val Pages = mapOf(
        EmptyPage.key to EmptyPage,

        HomePage.key to HomePage,

        LessonTablePage.key to LessonTablePage,
        DailyLessonPage.key to DailyLessonPage,

        AccountManagerPage.key to AccountManagerPage,
        EasLoginPage.key to EasLoginPage,
        IpassLoginPage.key to IpassLoginPage,

        QueryLessonPage.key to QueryLessonPage,
        QueryMarkPage.key to QueryMarkPage,
        QueryExamPage.key to QueryExamPage,
        QueryAcademicPage.key to QueryAcademicPage,
        QueryNoticePage.key to QueryNoticePage,

        DrinkPage.key to DrinkPage,

        ElectricRechargePage.key to ElectricRechargePage,
        SportTestPage.key to SportTestPage,

        WebPage.EasWebPage.key to WebPage.EasWebPage,
        WebPage.IpassWebPage.key to WebPage.IpassWebPage,

        UpdatePage.key to UpdatePage,
        AppPage.UserAgreementPage.key to AppPage.UserAgreementPage,
        AppPage.PolicyPage.key to AppPage.PolicyPage,

        SettingPage.key to SettingPage
    )

    val defaultPages: List<BasePage<*>> = listOf(LessonTablePage, MyPage)

    operator fun get(key: String?) = Pages[key]
}



