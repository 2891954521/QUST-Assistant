package com.qust.helper.ui.page.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.IconLogin
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.ui.widget.form.AccountInput
import com.qust.helper.viewmodel.account.IpassLoginViewModel
import kotlinx.coroutines.launch

object IpassLoginPage: BasePage<IpassLoginViewModel>("智慧青科大登陆", Drawables.IconLogin) {

	@Composable
	override fun getViewModel() = viewModel<IpassLoginViewModel>()

	@Composable
	override fun Content(viewModel: IpassLoginViewModel) {
		val scope = rememberCoroutineScope()
		val pageController = rememberPageController()
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
					viewModel.login { scope.launch { pageController.back() } }
				}
			)
			Button(modifier = Modifier.fillMaxWidth().padding(8.dp), onClick = {
				keyboardController?.hide()
				viewModel.login { scope.launch { pageController.back() } }
			}){
				Text(text = Strings.TEXT_OK)
			}
		}
	}
}

