package com.qust.helper.viewmodel.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.viewmodel.BaseViewModel

class AccountManagerViewModel: BaseViewModel() {

	var easAccountName by mutableStateOf(EasAccount.getAccountName())
	var iPassAccountName by mutableStateOf(IPassAccount.getAccountName())

	var askForLogoutEas by mutableStateOf(false)
	var askForLogoutIpass by mutableStateOf(false)

	fun easLogout(){
		runBackGround {
			EasAccount.logout()
			easAccountName = ""
		}
	}

	fun ipassLogout(){
		runBackGround {
			IPassAccount.logout()
			iPassAccountName = ""
		}
	}
}

