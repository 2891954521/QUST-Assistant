package com.qust.helper.viewmodel.account

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.viewmodel.BaseViewModel

abstract class AccountViewModel: BaseViewModel(){

	var errorText: MutableState<String> = mutableStateOf("")

	abstract fun login(accountStr: String, passwordStr: String, block: ()-> Unit);
}