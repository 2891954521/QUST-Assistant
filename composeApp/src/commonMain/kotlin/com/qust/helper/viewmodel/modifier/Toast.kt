package com.qust.helper.viewmodel.modifier

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.Res
import com.qust.helper.tips_error
import com.qust.helper.tips_finish
import com.qust.helper.tips_warning
import org.jetbrains.compose.resources.DrawableResource

data class ToastContent(
	val icon: DrawableResource = Res.drawable.tips_finish,
	val message: String = ""
){
	companion object {
		val EMPTY_TOAST: ToastContent = ToastContent()
	}
}

interface ToastAble {
	val toastContent: MutableState<ToastContent>
	fun toastOK(message: String)
	fun toastWarning(message: String)
	fun toastError(message: String)
}

class ToastAbleImpl(
	override val toastContent: MutableState<ToastContent> = mutableStateOf(ToastContent.EMPTY_TOAST)
): ToastAble {
	override fun toastOK(message: String){
		toastContent.value = ToastContent(Res.drawable.tips_finish, message)
	}
	override fun toastWarning(message: String) {
		toastContent.value = ToastContent(Res.drawable.tips_warning, message)
	}
	override fun toastError(message: String) {
		toastContent.value = ToastContent(Res.drawable.tips_error, message)
	}
}
