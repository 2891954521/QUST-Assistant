package com.qust.helper.viewmodel.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.account.EasAccount
import com.qust.helper.viewmodel.BaseViewModel

class AccountManagerViewModel: BaseViewModel() {

	var easAccountName by mutableStateOf(EasAccount.getAccountName())

	var askForLogoutEas by mutableStateOf(false)

	fun easLogout(){
//		EasAccount.logout()
		easAccountName = ""
	}
}

