package com.qust.helper.viewmodel.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.Keys
import com.qust.helper.model.account.EasAccount
import com.qust.helper.utils.SettingUtils
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK

class EasLoginViewModel : RequestViewModel() {

	val account = mutableStateOf(SettingUtils[Keys.EAS_ACCOUNT, ""])
	val password = mutableStateOf(SettingUtils[Keys.EAS_PASSWORD, ""])

	var accountError by mutableStateOf("")
	var passwordError by mutableStateOf("")

	fun login(){
		if(account.value.isEmpty()) { accountError = "请输入学号"; return }
		if(account.value.length < 2){ accountError = "学号格式错误"; return  }
		if(password.value.isEmpty()) { passwordError = "请输入密码"; return }

		accountError = ""
		passwordError = ""

		request({
			val result = EasAccount.login(account.value, password.value, true)
			if(result){
				toastOK("登录成功")
//				val year = Calendar.getInstance()[Calendar.YEAR].toString()
//				val currentYear = (year.substring(0, year.length - 2) + accountStr.substring(0, 2)).toInt()
//				easAccount.entranceTime = currentYear
//				block()
			}else{
				toastError("用户名或密码错误")
			}

		})
	}
}