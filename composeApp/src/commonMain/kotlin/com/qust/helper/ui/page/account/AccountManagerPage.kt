package com.qust.helper.ui.page.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.IconLogin
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.AskDialog
import com.qust.helper.viewmodel.account.AccountManagerViewModel


object AccountManagerPage: BasePage<AccountManagerViewModel>("账号管理", Drawables.IconLogin) {

	@Composable
	override fun getViewModel() = viewModel<AccountManagerViewModel>()

	@Composable
	override fun Content(viewModel: AccountManagerViewModel) {
		val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()

		val pageController = rememberPageController()

		Column {
			Card(modifier = Modifier.padding(8.dp)) {
				Text(text = "智慧青科大", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
				Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically){
					if(accountInfo.ipassAccount.isEmpty()){
						Text(text = "未登录", modifier = Modifier.weight(1F), color = colorSecondaryText)
						TextButton({  pageController.startPage(IpassLoginPage) }){
							Text(text = "点击登录", color = colorSecondaryText)
						}
					}else{
						Text(text = accountInfo.ipassAccount, modifier = Modifier.weight(1F))
						TextButton({  pageController.startPage(IpassLoginPage) }){
							Text(text = "更换账号", color = colorSecondaryText)
						}
						TextButton({ viewModel.askForLogoutIpass = true }){
							Text(text = "退出登录", color = MaterialTheme.colorScheme.error)
						}
					}
				}
			}

			Card(modifier = Modifier.padding(8.dp)) {
				Text(text = "教务系统", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
				Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
					if(accountInfo.easAccount.isEmpty()) {
						Text(text = "未登录", modifier = Modifier.weight(1F), color = colorSecondaryText)
						TextButton({ pageController.startPage(EasLoginPage) }) {
							Text(text = "点击登录", color = colorSecondaryText)
						}
					} else {
						Text(text = accountInfo.easAccount, modifier = Modifier.weight(1F))
						TextButton({ pageController.startPage(EasLoginPage) }) {
							Text(text = "更换账号", color = colorSecondaryText)
						}
						TextButton({ viewModel.askForLogoutEas = true }) {
							Text(text = "退出登录", color = MaterialTheme.colorScheme.error)
						}
					}
				}
			}
		}

		if(viewModel.askForLogoutEas){
			AskDialog("退出登录", "是否确定退出登录，储存的账号登录信息会被删除", { viewModel.askForLogoutEas = false }){
				viewModel.easLogout()
				viewModel.askForLogoutEas = false
			}
		}

		if(viewModel.askForLogoutIpass){
			AskDialog("退出登录", "是否确定退出登录，储存的账号登录信息会被删除", { viewModel.askForLogoutIpass = false }){
				viewModel.ipassLogout()
				viewModel.askForLogoutIpass = false
			}
		}
	}
}