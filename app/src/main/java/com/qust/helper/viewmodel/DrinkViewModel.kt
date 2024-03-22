package com.qust.helper.viewmodel

import androidx.compose.runtime.mutableStateOf
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

	var needLogin = mutableStateOf(false)

	val drinkAccountStr = mutableStateOf(Setting.getString(key = Keys.DRINK_ACCOUNT))
	val drinkPasswordStr = mutableStateOf(Setting.getString(key = Keys.DRINK_PASSWORD))

	val drinkCode = mutableStateOf(drinkAccount.drinkCode)

	fun login(){
		viewModelScope.launch {
			try {
				showDialog("登录中")
				val result = withContext(Dispatchers.IO){
					drinkAccount.login(drinkAccountStr.value, drinkPasswordStr.value, true)
				}
				if(result){
					getCode()
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
					drinkCode.value = drinkAccount.drinkCode
				}else{
					throw NeedLoginException()
				}
			}
		}catch(e: NeedLoginException){
			toastWarning("请先登录")
			needLogin.value = true

		}catch(e: Exception) {
			toastError("刷新失败: ${e.message}")

		}finally {
			clearDialog()
		}
	}
}