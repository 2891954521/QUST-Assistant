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
		override fun login(account: String, password: String){ this@DrinkViewModel.login(account, password) }
		override fun getDrinkCode(){ this@DrinkViewModel.getDrinkCode() }
	}

	fun login(account: String, password: String){
		viewModelScope.launch {
			try {
				showDialog("登录中")
				val result = withContext(Dispatchers.IO){
					drinkAccount.login(account, password, true)
				}
				if(result){
					getCode()
					uiState.account = account
					uiState.password = password
				}else{
					toastError("用户名或密码错误")
				}
			}catch(e: Exception) {
				toastError("网络错误: ${e.message}")
			}finally {
				clearDialog()
			}
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
					uiState.drinkCode = drinkAccount.drinkCode
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
	var drinkCode by mutableStateOf(drinkAccount.drinkCode)
	var account by mutableStateOf(Setting.getString(key = Keys.DRINK_ACCOUNT))
	var password by mutableStateOf(Setting.getString(key = Keys.DRINK_PASSWORD))
	var needLogin by mutableStateOf(false)
}

interface DrinkUIEvent{
	fun login(account: String, password: String){ }
	fun getDrinkCode(){ }
}