package com.qust.helper.next.common.setting

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

object JVMAppSetting {
    lateinit var preferences: Preferences
}

actual fun platformSettings(): Settings = PreferencesSettings(JVMAppSetting.preferences)