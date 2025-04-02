package com.qust.helper.viewmodel.extend

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf


interface LoadingAble {
	val _loadingText: MutableState<String>

	val loadingText: String

	fun showDialog(message: String)

	fun clearDialog()
}

class LoadingAbleImpl(
	override val _loadingText: MutableState<String> = mutableStateOf("")
): LoadingAble {

	override val loadingText by _loadingText

	override fun showDialog(message: String) {
		_loadingText.value = message
	}
	override fun clearDialog() {
		_loadingText.value = ""
	}
}