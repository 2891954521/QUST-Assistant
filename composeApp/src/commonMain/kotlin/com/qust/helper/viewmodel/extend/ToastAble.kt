package com.qust.helper.viewmodel.extend

import androidx.lifecycle.viewModelScope
import com.qust.helper.ui.widget.toast.ToastData
import com.qust.helper.ui.widget.toast.ToastUIState
import com.qust.helper.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ToastAble {
	val toastData: ToastUIState
}

fun BaseViewModel.toast(msg: String, type: ToastData.Type = ToastData.Type.NORMAL){
	viewModelScope.launch {
		withContext(Dispatchers.IO) {
			toastData.show(ToastData(msg, type))
		}
	}
}

fun BaseViewModel.toastOK(msg: String) = toast(msg, ToastData.Type.SUCCESS)

fun BaseViewModel.toastWarning(msg: String) = toast(msg, ToastData.Type.WARNING)

fun BaseViewModel.toastError(msg: String) = toast(msg, ToastData.Type.ERROR)