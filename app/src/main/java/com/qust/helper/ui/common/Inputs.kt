package com.qust.helper.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.qust.helper.R

object Inputs {

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

		val keyboardController = LocalSoftwareKeyboardController.current

		OutlinedTextField(
			value = account.value,
			onValueChange = { account.value = it },
			modifier = Modifier.fillMaxWidth().padding(32.dp, 8.dp, 32.dp, 0.dp),
			singleLine = true,
			label = { Text(text = labelAccount) },
			isError = accountError.isNotEmpty(),
			supportingText = { Text(text = accountError) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
			maxLines = 1
		)

		OutlinedTextField(
			value = password.value,
			onValueChange = { password.value = it },
			modifier = Modifier.fillMaxWidth().padding(32.dp, 4.dp, 32.dp, 0.dp),
			label = { Text(text = labelPassword) },
			isError = passwordError.isNotEmpty(),
			supportingText = { Text(text = passwordError) },
			trailingIcon = {
				IconButton(onClick = { passwordHidden = !passwordHidden }){
					Icon(painterResource(if(passwordHidden) R.drawable.ic_visibility else R.drawable.ic_visibility_off), null)
				}
			},
			visualTransformation = if(passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
			keyboardActions = KeyboardActions(onDone = { login() }),
			maxLines = 1
		)
	}
}