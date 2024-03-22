package com.qust.helper.viewmodel.account

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.Logger
import com.qust.helper.model.account.IPassAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class IPassAccountViewModel: AccountViewModel() {

	private val ipassAccount = IPassAccount.getInstance()

	val ipassName = mutableStateOf(Setting.getString(key = Keys.IPASS_ACCOUNT))
	val ipassPassword = mutableStateOf(Setting.getString(key = Keys.IPASS_PASSWORD))

	override fun login(accountStr: String, passwordStr: String, block: ()-> Unit) {
		viewModelScope.launch {
			try{
				dialogText.value = "登录中"

				val result = withContext(Dispatchers.IO){
					ipassAccount.login(accountStr, passwordStr, true)
				}

				if(result){
					block()
				}else{
					toastError("用户名或密码错误")
				}
			}catch(e: IOException){
				Logger.e("LoginFail", e)
				toastError("网络错误: " + e.message)
			}finally{
				dialogText.value = ""
			}
		}
	}
}