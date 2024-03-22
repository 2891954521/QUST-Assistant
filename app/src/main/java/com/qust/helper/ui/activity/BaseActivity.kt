package com.qust.helper.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.qust.helper.ui.common.ToastComponent
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.theme.AppTheme

abstract class BaseActivity: ComponentActivity() {

	abstract var toastContent: MutableState<ToastContent>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			AppTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					BaseContent()
				}
			}
		}
	}

	@Composable
	protected fun BaseContent(){
		Content()
		ToastComponent(toastContent)
	}

	@Composable
	protected open fun Content(){

	}

	fun toast(message: String) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
	}
}