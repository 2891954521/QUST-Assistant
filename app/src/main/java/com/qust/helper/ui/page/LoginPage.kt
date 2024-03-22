package com.qust.helper.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.common.Inputs
import com.qust.helper.viewmodel.account.EasAccountViewModel
import com.qust.helper.viewmodel.account.IPassAccountViewModel

object LoginPage {

	@Composable
	fun EASLoginPage(activity: ComponentActivity){
		val viewModel: EasAccountViewModel by activity.viewModels()
		EASLoginView(viewModel = viewModel, accountStr = viewModel.easName.value, passwordStr = viewModel.easPassword.value) {
			activity.onBackPressedDispatcher.onBackPressed()
		}
		AppBar.DialogAndToast(dialogText = viewModel.dialogText.value, toastContent = viewModel.toastContent)
	}

	@Composable
	fun IPassLoginPage(activity: ComponentActivity){
		val viewModel: IPassAccountViewModel by activity.viewModels()
		IPassLoginView(viewModel = viewModel, accountStr = viewModel.ipassName.value, passwordStr = viewModel.ipassPassword.value) {
			activity.onBackPressedDispatcher.onBackPressed()
		}
		AppBar.DialogAndToast(dialogText = viewModel.dialogText.value, toastContent = viewModel.toastContent)
	}

	@Composable
	fun EASLoginView(
		viewModel: EasAccountViewModel,
		accountStr: String = "",
		passwordStr: String = "",
		loginSuccess: () -> Unit
	){
		val errorText by viewModel.errorText

		val account = remember { mutableStateOf(accountStr) }
		val password = remember { mutableStateOf(passwordStr) }
		var accountError by remember { mutableStateOf("") }
		var passwordError by remember { mutableStateOf("") }

		val keyboardController = LocalSoftwareKeyboardController.current

		fun login(){
			keyboardController?.hide()
			if(account.value.isEmpty()) { accountError = "请输入学号"; return }
			else if(account.value.length < 2){ accountError = "学号格式错误"; return  }
			else accountError = ""
			if(password.value.isEmpty()) { passwordError = "请输入密码"; return }
			else passwordError = ""
			viewModel.login(account.value, password.value){
				loginSuccess()
			}
		}

		Inputs.AccountInput(
			account = account,
			password = password,
			accountError = errorText.ifEmpty { accountError },
			passwordError = passwordError,
			"学号", "教务系统密码"
		) {
			login()
		}

		Button(
			modifier = Modifier.fillMaxWidth().padding(32.dp, 16.dp, 32.dp, 0.dp),
			onClick = { login() }
		){
			Text(text = stringResource(id = R.string.text_ok))
		}
	}

	@Composable
	fun IPassLoginView(
		viewModel: IPassAccountViewModel,
		accountStr: String = "",
		passwordStr: String = "",
		loginSuccess: () -> Unit
	){
		val errorText by viewModel.errorText

		val account = remember { mutableStateOf(accountStr) }
		val password = remember{ mutableStateOf(passwordStr) }
		var accountError by remember { mutableStateOf("") }
		var passwordError by remember { mutableStateOf("") }

		val keyboardController = LocalSoftwareKeyboardController.current

		fun login(){
			keyboardController?.hide()
			if(account.value.isEmpty()) { accountError = "请输入学号"; return }
			else accountError = ""
			if(password.value.isEmpty()) { passwordError = "请输入密码"; return }
			else passwordError = ""
			viewModel.login(account.value, password.value){
				loginSuccess()
			}
		}

		Inputs.AccountInput(
			account = account,
			password = password,
			accountError = errorText.ifEmpty { accountError },
			passwordError = passwordError,
			"学号", "密码"
		) {
			login()
		}

		Button(
			modifier = Modifier.fillMaxWidth().padding(32.dp, 16.dp, 32.dp, 0.dp),
			onClick = { login() }
		){
			Text(text = stringResource(id = R.string.text_ok))
		}
	}
}

