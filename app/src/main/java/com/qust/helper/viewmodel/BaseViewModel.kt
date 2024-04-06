package com.qust.helper.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.qust.helper.R
import com.qust.helper.ui.widget.ToastContent

abstract class BaseViewModel: ViewModel() {
	open var dialogText = mutableStateOf("")
	open var toastContent = mutableStateOf(ToastContent())

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