package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.account.EASAccount

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

}