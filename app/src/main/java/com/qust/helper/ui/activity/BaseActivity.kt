package com.qust.helper.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.ui.widget.ToastAble
import com.qust.helper.ui.widget.ToastContent


abstract class BaseActivity: ComponentActivity() {

	abstract var toastContent: MutableState<ToastContent>

	private lateinit var toastAble: ToastAble

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

//		WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
//		WindowCompat.setDecorFitsSystemWindows(window, false)

		enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

		toastAble = ToastAble(this)

		setContent {
			AppTheme {
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					BaseContent()
				}
			}
		}
	}

	@Composable
	protected fun BaseContent(){
		LaunchedEffect(toastContent.value){
			val v = toastContent.value
			if(v.icon != 0 && v.message.isNotEmpty()){
				toastAble.toast(v.icon, v.message)
			}
		}
		Content()
	}

	@Composable
	protected open fun Content(){

	}

	fun androidToast(message: String) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
	}
}