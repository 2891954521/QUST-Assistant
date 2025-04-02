package com.qust.helper.ui.widget

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

interface DialogAble {
	val _dialogText: MutableState<String>
	val dialogText: String
	fun showDialog(message: String)
	fun clearDialog()
}

class DialogAbleImpl(override val _dialogText: MutableState<String> = mutableStateOf("")): DialogAble{
	override val dialogText: String
		get() = _dialogText.value

	override fun showDialog(message: String) {
		_dialogText.value = message
	}
	override fun clearDialog() {
		_dialogText.value = ""
	}
}