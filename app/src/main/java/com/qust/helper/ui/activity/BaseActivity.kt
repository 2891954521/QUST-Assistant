package com.qust.helper.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.qust.helper.ui.theme.Theme
import com.qust.helper.ui.widget.Toast


abstract class BaseActivity: ComponentActivity() {

	lateinit var toast: Toast

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

		setContent {
			Theme.AppTheme {
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					BaseContent()
				}
			}
		}

		toast = Toast(this)
	}

	@Composable
	protected fun BaseContent(){
		Content()
	}

	@Composable
	protected open fun Content(){

	}
}