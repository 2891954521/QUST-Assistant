package com.qust.helper.next.repository

import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.SettingKeys
import com.qust.helper.next.utils.DateUtils

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
}