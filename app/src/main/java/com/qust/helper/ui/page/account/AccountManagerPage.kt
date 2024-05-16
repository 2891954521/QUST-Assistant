package com.qust.helper.ui.page.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.data.Setting
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.ui.theme.Theme
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.Dialogs


object AccountManagerPage {

	val AccountManagerPage = Page(
		key = Keys.Page.AccountManager,
		name = "账号管理",
		iconRes = R.drawable.ic_login,
	) { _, padding, navController, _ ->

		var update by remember { mutableStateOf(false) }

		var iPassAccountName = IPassAccount.getInstance().getAccount()
		var easAccountName = EASAccount.getInstance().getAccount()

		LaunchedEffect(update){
			iPassAccountName = IPassAccount.getInstance().getAccount()
			easAccountName = EASAccount.getInstance().getAccount()
		}

		Box(Modifier.padding(padding)) {
			AccountManagerPageUI(
				iPassAccountName = iPassAccountName,
				easAccountName = easAccountName,
				go2EasLogin = {
					Page.navigate(navController, Keys.Page.EasLogin)
				},
				go2IpassLogin = {
					Page.navigate(navController, Keys.Page.VpnLoginPage)
				},
				easLogout = {
					EASAccount.getInstance().logout()
					update = !update
				},
				ipassLogout = {
					IPassAccount.getInstance().logout()
					update = !update
				},
			)
		}
	}

	@Composable
	fun AccountManagerPageUI(
		iPassAccountName: String = "",
		easAccountName: String = "",
		go2EasLogin: () -> Unit = { },
		go2IpassLogin: () -> Unit = { },
		easLogout: () -> Unit = { },
		ipassLogout: () -> Unit = { },
	){
		var askForLogoutEas by remember { mutableStateOf(false) }
		var askForLogoutIpass by remember { mutableStateOf(false) }

		Column {
			Card(modifier = Modifier.padding(8.dp)) {
				Text(text = "智慧青科大", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
				Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically){
					if(iPassAccountName.isEmpty()){
						Text(text = "未登录", modifier = Modifier.weight(1F), color = colorSecondaryText)
						TextButton({  go2IpassLogin() }){
							Text(text = "点击登录", color = colorSecondaryText)
						}
					}else{
						Text(text = iPassAccountName, modifier = Modifier.weight(1F))
						TextButton({  go2IpassLogin() }){
							Text(text = "更换账号", color = colorSecondaryText)
						}
						TextButton({ askForLogoutIpass = true }){
							Text(text = "退出登录", color = MaterialTheme.colorScheme.error)
						}
					}
				}
			}

			Card(modifier = Modifier.padding(8.dp)) {
				Text(text = "教务系统", fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
				Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
					if(easAccountName.isEmpty()) {
						Text(text = "未登录", modifier = Modifier.weight(1F), color = colorSecondaryText)
						TextButton({ go2EasLogin() }) {
							Text(text = "点击登录", color = colorSecondaryText)
						}
					} else {
						Text(text = easAccountName, modifier = Modifier.weight(1F))
						TextButton({ go2EasLogin() }) {
							Text(text = "更换账号", color = colorSecondaryText)
						}
						TextButton({ askForLogoutEas = true }) {
							Text(text = "退出登录", color = MaterialTheme.colorScheme.error)
						}
					}
				}
			}

			if(askForLogoutEas){
				Dialogs.AskDialog("退出登录", "是否确定退出登录，储存的账号登录信息会被删除", { askForLogoutEas = false }){
					easLogout()
					askForLogoutEas = false
				}
			}

			if(askForLogoutIpass){
				Dialogs.AskDialog("退出登录", "是否确定退出登录，储存的账号登录信息会被删除", { askForLogoutIpass = false }){
					ipassLogout()
					askForLogoutIpass = false
				}
			}

		}
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun my() {
	Setting.init(LocalContext.current)
	Theme.AppTheme {
		AccountManagerPage.AccountManagerPageUI()
	}
}