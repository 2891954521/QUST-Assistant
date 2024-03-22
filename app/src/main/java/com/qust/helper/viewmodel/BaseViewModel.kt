package com.qust.helper.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.qust.helper.R
import com.qust.helper.ui.common.ToastContent

abstract class BaseViewModel: ViewModel() {
	open var dialogText: MutableState<String> = mutableStateOf("")
	open var toastContent: MutableState<ToastContent> = mutableStateOf(ToastContent())

	fun toastOK(message: String){
		toastContent.value = ToastContent(R.drawable.tips_finish, message)
	}

	fun toastWarning(message: String) {
		toastContent.value = ToastContent(R.drawable.tips_warning, message)
	}

	fun toastError(message: String) {
		toastContent.value = ToastContent(R.drawable.tips_error, message)
	}

	fun showDialog(message: String){
		dialogText.value = message
	}

	fun clearDialog(){
		dialogText.value = ""
	}
}