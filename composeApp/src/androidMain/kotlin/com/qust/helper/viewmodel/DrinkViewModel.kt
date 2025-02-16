package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.account.DrinkAccount
import com.qust.helper.model.account.NeedLoginException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DrinkViewModel: BaseViewModel() {

	private val drinkAccount: DrinkAccount = DrinkAccount.getInstance()

	val uiState = DrinkUIState(drinkAccount = drinkAccount)

	val uiEvent = object : DrinkUIEvent{
		override fun login(){ this@DrinkViewModel.login() }
		override fun getDrinkCode(){ this@DrinkViewModel.getDrinkCode() }
	}

	fun login(){
		viewModelScope.launch {
			showDialog("登录中")
			try {
				val result = withContext(Dispatchers.IO){
					drinkAccount.login(uiState.account.value, uiState.password.value, true)
				}
				if(result) getCode()
				else toastError("用户名或密码错误")
			}catch(e: Exception) {
				toastError("网络错误: ${e.message}")
			}
			clearDialog()
		}
	}

	fun getDrinkCode(){
		viewModelScope.launch { getCode() }
	}

	private suspend fun getCode(){
		try {
			showDialog("正在刷新")
			withContext(Dispatchers.IO) {
				if(drinkAccount.checkLogin()){
					drinkAccount.getDrinkCode()
				}else{
					throw NeedLoginException()
				}
			}
		}catch(e: NeedLoginException){
			toastWarning("请先登录")
			uiState.needLogin = true

		}catch(e: Exception) {
			toastError("刷新失败: ${e.message}")

		}finally {
			clearDialog()
		}
	}
}

class DrinkUIState(drinkAccount: DrinkAccount) {
	var drinkCode by drinkAccount._drinkCode
	var account = mutableStateOf(Setting.getString(key = Keys.DRINK_ACCOUNT))
	var password = mutableStateOf(Setting.getString(key = Keys.DRINK_PASSWORD))
	var needLogin by mutableStateOf(false)
}

interface DrinkUIEvent{
	fun login(){ }
	fun getDrinkCode(){ }
}