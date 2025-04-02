package com.qust.helper.data

import androidx.compose.runtime.mutableStateOf
import com.qust.helper.utils.SettingUtils
import java.io.File

object Settings {


	val themeDark = mutableStateOf(SettingUtils[Keys.KEY_THEME_DARK, false])

	val themeFollowSystem = mutableStateOf(SettingUtils[Keys.KEY_THEME_FOLLOW_SYSTEM, true])

	val lessonTableFolder = File("")
}