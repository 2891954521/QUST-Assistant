package com.qust.helper.model

import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Keys
import com.qust.helper.utils.SettingUtils
import java.io.File

object SettingModel {


	val themeDark = mutableStateOf(SettingUtils[Keys.KEY_THEME_DARK, false])

	val themeFollowSystem = mutableStateOf(SettingUtils[Keys.KEY_THEME_FOLLOW_SYSTEM, true])

	val lessonTableFolder = File("")


	var totalWeek = SettingUtils[Keys.SETTING_TOTAL_WEEK, 1]
		set(value) {
			SettingUtils[Keys.SETTING_TOTAL_WEEK] = value
			field = value
		}
}