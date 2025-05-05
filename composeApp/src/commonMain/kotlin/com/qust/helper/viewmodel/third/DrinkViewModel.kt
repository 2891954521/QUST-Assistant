package com.qust.helper.viewmodel.third


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.Keys
import com.qust.helper.model.account.DrinkAccount
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.utils.SettingUtils
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastWarning

class DrinkViewModel : RequestViewModel(), DrinkUIEvent {

	private val drinkAccount = DrinkAccount

	val uiState = DrinkUIState(drinkAccount = drinkAccount)

	override fun login() {
		request({
			try {
				val result = drinkAccount.login(uiState.account.value, uiState.password.value, true)
				if(result) drinkAccount.getDrinkCode()
				else toastError("用户名或密码错误")
			} catch(e: Exception) {
				toastError("网络错误: ${e.message}")
			}
		})
	}

	override fun getDrinkCode() {
		request({
			try {
				drinkAccount.getDrinkCode()
			} catch(e: NeedLoginException) {
				toastWarning("请先登录")
				uiState.needLogin = true
			} catch(e: Exception) {
				toastError("刷新失败: ${e.message}")
			}
		})
	}
}

class DrinkUIState(drinkAccount: DrinkAccount) {
	var drinkCode by drinkAccount._drinkCode
	var account = mutableStateOf(SettingUtils.getString(key = Keys.DRINK_ACCOUNT))
	var password = mutableStateOf(SettingUtils.getString(key = Keys.DRINK_PASSWORD))
	var needLogin by mutableStateOf(false)
}

interface DrinkUIEvent {
	fun login() {}
	fun getDrinkCode() {}
}