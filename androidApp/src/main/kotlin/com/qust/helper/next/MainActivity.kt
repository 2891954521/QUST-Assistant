package com.qust.helper.next

import com.qust.helper.next.ui.activity.BasePageActivity
import com.qust.helper.next.ui.page.HomePage
import com.qust.helper.next.ui.page.base.BasePage

class MainActivity : BasePageActivity() {

    val mainPage: BasePage<*> = HomePage()

    override fun getPage(): BasePage<*> = mainPage
}