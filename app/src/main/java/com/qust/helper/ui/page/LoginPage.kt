package com.qust.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.ui.widget.AppBar
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.account.AccountViewModel
import com.qust.helper.viewmodel.account.EasAccountViewModel
import com.qust.helper.viewmodel.account.IPassAccountViewModel

object LoginPage {

	@Composable
	fun EASLoginPage(padding: PaddingValues, viewModel: EasAccountViewModel, toast: Toast, onSuccess: () -> Unit = { }){
		Box(modifier = Modifier.padding(padding)){
			EASLoginView(
				viewModel = viewModel,
				accountStr = viewModel.easName.value,
				passwordStr = viewModel.easPassword.value,
				loginSuccess = onSuccess
			)
		}
		AppBar.DialogBar(viewModel.dialogText)
		toast.ToastContent(viewModel.toastContent)
	}

	@Composable
	fun IPassLoginPage(padding: PaddingValues, viewModel: IPassAccountViewModel, toast: Toast, onSuccess: () -> Unit = { }){
		Box(modifier = Modifier.padding(padding)) {
			IPassLoginView(
				viewModel = viewModel,
				accountStr = viewModel.ipassName.value,
				passwordStr = viewModel.ipassPassword.value,
				loginSuccess = onSuccess
			)
		}
		AppBar.DialogBar(viewModel.dialogText)
		toast.ToastContent(viewModel.toastContent)
	}

	@Composable
	fun EASLoginView(
		viewModel: AccountViewModel,
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

		Column(Modifier.padding(horizontal = 16.dp)) {
			AccountInput(
				account = account,
				password = password,
				accountError = errorText.ifEmpty { accountError },
				passwordError = passwordError,
				"学号", "教务系统密码",
				login = { login() }
			)
			Button(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = { login() }){
				Text(text = stringResource(id = R.string.text_ok))
			}
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

		Column(Modifier.padding(horizontal = 16.dp)) {
			AccountInput(
				account = account,
				password = password,
				accountError = errorText.ifEmpty { accountError },
				passwordError = passwordError,
				"学号", "密码",
				login = { login() }
			)

			Button(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = { login() }){
				Text(text = stringResource(id = R.string.text_ok))
			}
		}
	}

	@Composable
	fun AccountInput(
		account: MutableState<String>,
		password: MutableState<String>,
		accountError: String = "",
		passwordError: String = "",
		labelAccount: String = "账号",
		labelPassword: String = "密码",
		login: () -> Unit
	){
		var passwordHidden by remember{ mutableStateOf(true) }

		OutlinedTextField(
			value = account.value,
			onValueChange = { account.value = it },
			modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
			singleLine = true,
			label = { Text(text = labelAccount) },
			isError = accountError.isNotEmpty(),
			supportingText = { if(accountError.isNotEmpty()) Text(text = accountError) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
			maxLines = 1
		)

		OutlinedTextField(
			value = password.value,
			onValueChange = { password.value = it },
			modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
			label = { Text(text = labelPassword) },
			isError = passwordError.isNotEmpty(),
			supportingText = { if(passwordError.isNotEmpty()) Text(text = passwordError) },
			trailingIcon = {
				IconButton(onClick = { passwordHidden = !passwordHidden }){
					Icon(painterResource(if(passwordHidden) R.drawable.ic_visibility else R.drawable.ic_visibility_off), null)
				}
			},
			visualTransformation = if(passwordHidden) PasswordVisualTransformation('*') else VisualTransformation.None,
			keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
			keyboardActions = KeyboardActions(onDone = { login() }),
			maxLines = 1
		)
	}
}

