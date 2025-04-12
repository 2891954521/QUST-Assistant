package com.qust.helper.viewmodel.app

import androidx.compose.runtime.getValue
import com.qust.helper.model.lessonTable.LessonTableModel
import com.qust.helper.viewmodel.BaseViewModel

class SettingViewModel: BaseViewModel() {

	val totalWeek by LessonTableModel._totalWeek
	fun setTotalWeek(week: Int){

	}


//	val entranceTime by mutableStateOf(EASAccount.getInstance().entranceTime.toString())
//	fun setEntranceTimeValue(value: Int) {
//		EASAccount.getInstance().entranceTime = value
//		entranceTime = value.toString()
//	}

//	val eaHost by mutableIntStateOf(SettingUtils[Keys.EA_HOST, 0])
//	fun setEaHostValue(value: Int){
//		EASAccount.getInstance().changeHost(value)
//		eaHost = value
//	}

//	var eaUseVpn by mutableStateOf(SettingUtils.getBoolean(Keys.EA_USE_VPN, false)); private set
//	fun setEaUseVpnValue(value: Boolean){
//		EASAccount.useVpn = value
//		SettingUtils.putBoolean(Keys.EA_USE_VPN, value)
//		eaUseVpn = value
//	}

//	var themeFollowSystem by SettingModel.themeFollowSystem; private set
//	fun setThemeFollowSystemValue(value: Boolean){
//		SettingUtils.putBoolean(Keys.KEY_THEME_FOLLOW_SYSTEM, value)
//		themeFollowSystem = value
//	}
//	var themeDark by SettingModel.themeDark; private set
//	fun setThemeDarkValue(value: Boolean){
//		SettingUtils.putBoolean(Keys.KEY_THEME_DARK, value)
//		themeDark = value
//	}

//	var autoUpdate by mutableStateOf(SettingUtils.getBoolean(Keys.KEY_AUTO_UPDATE, true)); private set
//	fun setAutoUpdateValue(value: Boolean){
//		SettingUtils.putBoolean(Keys.KEY_AUTO_UPDATE, value)
//		autoUpdate = value
//	}

//	val buildTime by mutableStateOf(BuildConfig.PACKAGE_TIME)
//	val appVersion by mutableStateOf(BuildConfig.VERSION_NAME)
}