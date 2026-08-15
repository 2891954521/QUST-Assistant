package com.qust.helper.next.ui.page.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.qust.helper.next.App
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.repository.AccountRepository
import com.qust.helper.next.ui.business.login.AccountInput
import com.qust.helper.next.ui.drawables.Drawables
import com.qust.helper.next.ui.drawables.IconLogin
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

object IpassLoginPage: AppPage<IpassLoginViewModel>(
	title = "智慧青科大登陆",
	icon = Drawables.IconLogin,
	viewModelClass = IpassLoginViewModel::class
) {

	@Composable
	override fun Content(viewModel: IpassLoginViewModel) {
		val scope = rememberCoroutineScope()
		val router = App.router
		val keyboardController = LocalSoftwareKeyboardController.current

		Column(Modifier.padding(horizontal = 16.dp)) {
			AccountInput(
				account = viewModel.account,
				password = viewModel.password,
				accountError = viewModel.accountError,
				passwordError = viewModel.passwordError,
				"学号",
				"智慧青科大密码",
				login = {
					keyboardController?.hide()
					viewModel.login { scope.launch { router.back() } }
				}
			)
			Button(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = {
				keyboardController?.hide()
				viewModel.login { scope.launch { router.back() } }
			}){
				Text(text = Strings.TEXT_OK)
			}
		}
	}
}

class IpassLoginViewModel: BaseViewModel() {

	val account = mutableStateOf(AppSetting[DataKeys.IPASS_ACCOUNT, ""])
	val password = mutableStateOf(AppSetting[DataKeys.IPASS_PASSWORD, ""])

	var accountError by mutableStateOf("")
	var passwordError by mutableStateOf("")

	fun login(onLogin: () -> Unit){
		if(account.value.isEmpty()) { accountError = "请输入学号"; return }
		if(account.value.length < 2){ accountError = "学号格式错误"; return  }
		if(password.value.isEmpty()) { passwordError = "请输入密码"; return }

		accountError = ""
		passwordError = ""

		loading {
			val result = AccountRepository.ipassLogin(account.value, password.value)
			if(result){
				toastSuccess("登录成功")
				onLogin()
			}else{
				toastError("用户名或密码错误")
			}
		}
	}
}

