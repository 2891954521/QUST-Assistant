package com.qust.helper.viewmodel.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.account.EasAccount
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.Logger
import com.qust.helper.viewmodel.BaseViewModel
import com.qust.helper.viewmodel.extend.toastWarning

class SettingViewModel: BaseViewModel() {

	var totalWeek by mutableIntStateOf(LessonTableRepository.currentLessonTable.value.totalWeek)
	fun setTotalWeek(weekStr: String){
		val week = weekStr.toIntOrNull()
		if(week == null){
			toastWarning("请输入正确的数字")
			return
		}
		totalWeek = week
		LessonTableRepository.updateTotalWeek(week)
	}

	val _startDay = mutableStateOf(DateUtils.YMD.format(LessonTableRepository.currentLessonTable.value.startDay))
	val startDay by _startDay
	fun setStartDay(startDayStr: String){
		try {
			_startDay.value = startDayStr
			LessonTableRepository.updateStartDay(DateUtils.YMD.parse(startDayStr))
		} catch(e: Exception) {
			Logger.e("", e)
			toastWarning("请输入正确的日期")
		}
	}

	val entranceTime = mutableStateOf(EasAccount.entranceDate.toString())
	fun setEntranceTime(value: Int) {
		entranceTime.value = value.toString()
		EasAccount.entranceDate = value
	}

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