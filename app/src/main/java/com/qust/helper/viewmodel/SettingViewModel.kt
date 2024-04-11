package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.qust.helper.BuildConfig
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.account.EASAccount
import com.qust.helper.ui.theme.Theme

class SettingViewModel: ViewModel() {

	var lockLesson: Boolean = Setting.getBoolean(Keys.KEY_LOCK_LESSON, false)
		set(value) {
			Setting.edit { it.putBoolean(Keys.KEY_LOCK_LESSON, value) }
//			lessonTableViewModel.
			field = value
		}

	var entranceTime by mutableStateOf(EASAccount.getInstance().entranceTime.toString())
		private set

	fun setEntranceTimeValue(value: Int) {
		EASAccount.getInstance().entranceTime = value
		entranceTime = value.toString()
	}

	var eaHost by mutableIntStateOf(Setting.getInt(Keys.EA_HOST, 0))
		private set

	fun setEaHostValue(value: Int){
		EASAccount.getInstance().changeHost(value)
		eaHost = value
	}


	var themeFollowSystem by Theme.themeFollowSystem; private set
	fun setThemeFollowSystemValue(value: Boolean){
		Setting.edit { it.putBoolean(Keys.KEY_THEME_FOLLOW_SYSTEM, value) }
		themeFollowSystem = value
	}
	var themeDark by Theme.themeDark; private set
	fun setThemeDarkValue(value: Boolean){
		Setting.edit { it.putBoolean(Keys.KEY_THEME_DARK, value) }
		themeDark = value
	}

	var autoUpdate by mutableStateOf(Setting.getBoolean(Keys.KEY_AUTO_UPDATE, true)); private set
	fun setAutoUpdateValue(value: Boolean){
		Setting.edit { it.putBoolean(Keys.KEY_AUTO_UPDATE, value) }
		autoUpdate = value
	}

	val buildTime by mutableStateOf(BuildConfig.PACKAGE_TIME)
	val appVersion by mutableStateOf(BuildConfig.VERSION_NAME)
}