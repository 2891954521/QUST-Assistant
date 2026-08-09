package com.qust.helper.next.common.setting

import com.russhwolf.settings.Settings

object AppSetting: Settings by platformSettings()

expect fun platformSettings(): Settings
