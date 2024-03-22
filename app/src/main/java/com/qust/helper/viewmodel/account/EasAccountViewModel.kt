package com.qust.helper.viewmodel.account

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.Logger
import com.qust.helper.model.account.EASAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Calendar

class EasAccountViewModel: AccountViewModel(){

	private val easAccount = EASAccount.getInstance()

	val easName = mutableStateOf(Setting.getString(key = Keys.EAS_ACCOUNT))
	val easPassword = mutableStateOf(Setting.getString(key = Keys.EAS_PASSWORD))

	override fun login(accountStr: String, passwordStr: String, block: ()-> Unit) {
		viewModelScope.launch {
			try{
				dialogText.value = "登录中"

				val result = withContext(Dispatchers.IO){
					easAccount.login(accountStr, passwordStr, true)
				}

				if(result){
					val year = Calendar.getInstance()[Calendar.YEAR].toString()
					val currentYear = (year.substring(0, year.length - 2) + accountStr.substring(0, 2)).toInt()
					easAccount.entranceTime = currentYear
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