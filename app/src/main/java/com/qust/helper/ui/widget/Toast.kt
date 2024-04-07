package com.qust.helper.ui.widget

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Toast(context: Context){
	private var isShowToast = false

	private var toastWindow = context.getSystemService(ComponentActivity.WINDOW_SERVICE) as WindowManager

	private var toastLayout = LayoutInflater.from(context).inflate(R.layout.layout_tips, null) as LinearLayout

	private var toastIcon = toastLayout.findViewById<ImageView>(R.id.tips_icon)
	private var toastMessage = toastLayout.findViewById<TextView>(R.id.tips_message)

	private var toastParams = WindowManager.LayoutParams().also {
		it.gravity = Gravity.CENTER
		it.format = PixelFormat.TRANSLUCENT
		it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
		it.width = WindowManager.LayoutParams.WRAP_CONTENT
		it.height = WindowManager.LayoutParams.WRAP_CONTENT
		it.windowAnimations = android.R.style.Animation_Toast
	}

	private fun toast(icon: Int, message: String){
		toastIcon.setImageResource(icon)
		toastMessage.text = message
		if(!isShowToast){
			isShowToast = true
			toastWindow.addView(toastLayout, toastParams)
			CoroutineScope(Dispatchers.Main).launch {
				delay(3000)
				toastWindow.removeView(toastLayout)
				isShowToast = false
			}
		}
	}

	fun toastOK(message: String){ toast(R.drawable.tips_finish, message) }
	fun toastWarning(message: String) { toast(R.drawable.tips_warning, message) }
	fun toastError(message: String) { toast(R.drawable.tips_error, message) }

	@Composable
	fun ToastContent(toastContent: MutableState<ToastContent>){
		LaunchedEffect(toastContent.value){
			val v = toastContent.value
			if(v.icon != 0 && v.message.isNotEmpty()){
				toast(v.icon, v.message)
				toastContent.value = ToastContent.EMPTY_TOAST
			}
		}
	}
}

interface ToastAble {
	val toastContent: MutableState<ToastContent>
	fun toastOK(message: String)
	fun toastWarning(message: String)
	fun toastError(message: String)
}

class ToastAbleImpl(override val toastContent: MutableState<ToastContent> = mutableStateOf(ToastContent.EMPTY_TOAST)): ToastAble{
	override fun toastOK(message: String){
		toastContent.value = ToastContent(R.drawable.tips_finish, message)
	}
	override fun toastWarning(message: String) {
		toastContent.value = ToastContent(R.drawable.tips_warning, message)
	}
	override fun toastError(message: String) {
		toastContent.value = ToastContent(R.drawable.tips_error, message)
	}
}

class ToastContent(var icon: Int = 0, val message: String = ""){
	companion object {
		val EMPTY_TOAST: ToastContent = ToastContent()
	}
}