package com.qust.helper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qust.helper.ui.widget.toast.ToastUIState
import com.qust.helper.viewmodel.extend.LoadingAble
import com.qust.helper.viewmodel.extend.LoadingAbleImpl
import com.qust.helper.viewmodel.extend.ToastAble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel: ViewModel(), ToastAble, LoadingAble by LoadingAbleImpl() {

	override val toastData: ToastUIState = ToastUIState()

	fun runBackGround(block: suspend () -> Unit){
		viewModelScope.launch {
			withContext(Dispatchers.IO){
				block()
			}
		}
	}
}

