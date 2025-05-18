package com.qust.helper.viewmodel.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.Keys
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.utils.SettingUtils
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK

class IpassLoginViewModel : RequestViewModel() {

	val account = mutableStateOf(SettingUtils[Keys.IPASS_ACCOUNT, ""])
	val password = mutableStateOf(SettingUtils[Keys.IPASS_PASSWORD, ""])

	var accountError by mutableStateOf("")
	var passwordError by mutableStateOf("")

	fun login(onLogin: () -> Unit){
		if(account.value.isEmpty()) { accountError = "请输入学号"; return }
		if(account.value.length < 2){ accountError = "学号格式错误"; return  }
		if(password.value.isEmpty()) { passwordError = "请输入密码"; return }

		accountError = ""
		passwordError = ""

		request({
			val result = IPassAccount.login(account.value, password.value, true)
			if(result){
				toastOK("登录成功")
				onLogin()
			}else{
				toastError("用户名或密码错误")
			}
		})
	}
}