package com.qust.helper.next.ui.page.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.App
import com.qust.helper.next.repository.AccountRepository
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.ConfirmDialog
import com.qust.helper.next.ui.component.button.TextButton
import com.qust.helper.next.ui.component.button.TextButtonColors
import com.qust.helper.next.ui.drawables.Drawables
import com.qust.helper.next.ui.drawables.IconLogin
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.BaseViewModel


@Preview
@Composable
private fun AccountManagerPreview() = AppPreview(::AccountManagerUI)

class AccountManagerPage: AppPage<AccountManagerViewModel>(
	title = "账号管理",
	icon = Drawables.IconLogin,
	viewModelClass = AccountManagerViewModel::class
) {
	@Composable
	override fun Content(viewModel: AccountManagerViewModel) = AccountManagerUI(viewModel)
}

@Composable
private fun AccountManagerUI(viewModel: AccountManagerViewModel) {
	val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()

	val router = App.router

	var askForLogoutIpass by remember { mutableStateOf(false) }
	var askForLogoutEas by remember { mutableStateOf(false) }

	Column {
		Card(modifier = Modifier.padding(8.dp)) {
			Text(text = "智慧青科大", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
			Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically){
				if(accountInfo.ipassAccount.isEmpty()){
					Text(text = "未登录", modifier = Modifier.weight(1F), color = Theme.color.textSecondary)
					TextButton(text = "点击登录"){  router.startPage<IpassLoginPage>() }
				}else{
					Text(text = accountInfo.ipassAccount, modifier = Modifier.weight(1F))
					TextButton(text = "更换账号", colors = TextButtonColors.Secondary){  router.startPage<IpassLoginPage>() }
					TextButton(text = "退出登录", colors = TextButtonColors.Danger){ askForLogoutIpass = true }
				}
			}
		}

		Card(modifier = Modifier.padding(8.dp)) {
			Text(text = "教务系统", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
			Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
				if(accountInfo.easAccount.isEmpty()) {
					Text(text = "未登录", modifier = Modifier.weight(1F), color = Theme.color.textSecondary)
					TextButton(text = "点击登录"){ router.startPage<EasLoginPage>() }
				} else {
					Text(text = accountInfo.easAccount, modifier = Modifier.weight(1F))
					TextButton(text = "更换账号"){ router.startPage<EasLoginPage>() }
					TextButton(text = "退出登录", colors = TextButtonColors.Danger){ askForLogoutEas = true }
				}
			}
		}
	}

	ConfirmDialog(
		visible = askForLogoutEas,
		title = "退出登录", 
		message = "是否确定退出登录，储存的账号登录信息会被删除",
		onDismiss = { askForLogoutEas = false },
		onConfirm = {
			viewModel.easLogout()
			askForLogoutEas = false
		}
	)

	ConfirmDialog(
		visible = askForLogoutIpass,
		title = "退出登录",
		message = "是否确定退出登录，储存的账号登录信息会被删除",
		onDismiss = { askForLogoutIpass = false },
		onConfirm = {
			viewModel.ipassLogout()
			askForLogoutIpass = false
		}
	)
}

class AccountManagerViewModel : BaseViewModel() {
	val accountInfo = AccountRepository.qustAccountInfo
	fun easLogout(){
		runInBackground {
			AccountRepository.easLogout()
		}
	}

	fun ipassLogout(){
		runInBackground {
			AccountRepository.ipassLogout()
		}
	}
}