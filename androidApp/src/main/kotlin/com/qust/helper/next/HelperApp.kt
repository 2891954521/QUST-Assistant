package com.qust.helper.next

import com.qust.helper.next.ui.router.ActivityCollector

class HelperApp: BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        ActivityCollector.pageActivityClass = PageActivity::class.java
    }
}