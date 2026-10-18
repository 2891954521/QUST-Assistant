package com.qust.helper.next.common.setting

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

object JVMAppSetting {

    private var _preferences: Preferences? = null

    var preferences: Preferences
        get() {
            if(_preferences == null) _preferences = Preferences.userRoot().node("com.qust.helper.next")
            return _preferences!!
        }
        set(value) {
            _preferences = value
        }

}

actual fun platformSettings(): Settings = PreferencesSettings(JVMAppSetting.preferences)