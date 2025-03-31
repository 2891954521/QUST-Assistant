package com.qust.helper.viewmodel

import androidx.lifecycle.ViewModel
import com.qust.helper.ui.widget.toast.ToastUIState
import com.qust.helper.viewmodel.extend.ToastAble

open class BaseViewModel: ViewModel(), ToastAble {

	override val toastData: ToastUIState = ToastUIState()
}

