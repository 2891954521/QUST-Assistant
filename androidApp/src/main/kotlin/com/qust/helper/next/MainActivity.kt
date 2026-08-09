package com.qust.helper.next

import com.qust.helper.next.ui.activity.BasePageActivity
import com.qust.helper.next.ui.page.MainPage
import com.qust.helper.next.ui.page.base.BasePage

class MainActivity : BasePageActivity() {

    val mainPage: BasePage<*> = MainPage()

    override fun getPage(): BasePage<*> = mainPage
}