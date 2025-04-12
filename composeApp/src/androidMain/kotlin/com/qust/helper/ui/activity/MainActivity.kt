package com.qust.helper.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qust.helper.ui.page.HomePage

class MainActivity: ComponentActivity() {

	val page = HomePage

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { HomePage.ComposePage() }
	}
}