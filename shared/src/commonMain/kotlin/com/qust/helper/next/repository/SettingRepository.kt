package com.qust.helper.next.repository

import com.qust.helper.next.common.Platform
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.SettingKeys
import com.qust.helper.next.utils.DateUtils
import com.sun.tools.javac.code.TypeAnnotationPosition.field
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SettingRepository {

	var totalWeek = AppSetting[SettingKeys.SETTING_TOTAL_WEEK, 1]
		set(value) {
			AppSetting[SettingKeys.SETTING_TOTAL_WEEK] = value
			field = value
		}

	var startDay = AppSetting.getString(SettingKeys.SETTING_START_DAY, "").let {
		if(it.isBlank()) DateUtils.today() else DateUtils.YMD.parse(it)
	}
		set(value) {
			AppSetting[SettingKeys.SETTING_START_DAY] = DateUtils.YMD.format(value)
			field = value
		}



	val uiSetting: StateFlow<UISetting> field = MutableStateFlow(UISetting())

	fun setUiSetting(setting: UISetting) {
		AppSetting[SettingKeys.SETTING_UI_PERCENTAGE_LAYOUT] = setting.percentageLayout
		AppSetting[SettingKeys.SETTING_UI_UI_SCALE] = setting.uiScale
		AppSetting[SettingKeys.SETTING_UI_FONT_SCALE] = setting.fontScale
		uiSetting.value = setting
	}


	data class UISetting(
		/**
		 * 使用百分比布局
		 */
		val percentageLayout: Boolean = AppSetting[SettingKeys.SETTING_UI_PERCENTAGE_LAYOUT, { Platform.currentPlatform() == Platform.PlatformType.ANDROID }],
		val uiScale: Float = AppSetting.getFloat(SettingKeys.SETTING_UI_UI_SCALE, 1F),
		val fontScale: Float = AppSetting.getFloat(SettingKeys.SETTING_UI_FONT_SCALE, 1F),
	)
}