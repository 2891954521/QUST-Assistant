package com.qust.helper.viewmodel.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.repository.AccountRepository
import com.qust.helper.viewmodel.BaseViewModel

class AccountManagerViewModel: BaseViewModel() {

	val accountInfo = AccountRepository.qustAccountInfo

	var askForLogoutEas by mutableStateOf(false)
	var askForLogoutIpass by mutableStateOf(false)

	fun easLogout(){
		runBackGround {
			AccountRepository.easLogout()
		}
	}

	fun ipassLogout(){
		runBackGround {
			AccountRepository.ipassLogout()
		}
	}
}

