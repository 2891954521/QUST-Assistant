package com.qust.helper.next

import com.qust.helper.next.common.setting.JVMAppSetting
import com.qust.helper.next.ui.page.gui
import com.qust.helper.next.ui.router.DesktopPageController
import com.qust.helper.next.ui.page.MainPage
import java.util.prefs.Preferences

fun main() {
    JVMAppSetting.preferences = Preferences.userRoot().node("com.qust.helper.next")

    DesktopPageController.startPage<MainPage>()

    gui()
}