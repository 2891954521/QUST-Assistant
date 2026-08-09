package com.qust.helper.next.common.setting

import android.content.SharedPreferences
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.qust.helper.next.common.log.Logger

object AndroidAppSetting {
    var defaultSharedPreferences: SharedPreferences? = null
}

actual fun platformSettings(): Settings {
    val sp = AndroidAppSetting.defaultSharedPreferences
    if(sp == null){
        Logger.e("defaultSharedPreferences hasn't init. AppSettings using MemorySettings")
        return MemorySetting()
    }else{
        return SharedPreferencesSettings(sp)
    }
}
