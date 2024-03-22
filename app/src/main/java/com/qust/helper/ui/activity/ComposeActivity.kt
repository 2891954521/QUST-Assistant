package com.qust.helper.ui.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Data
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.common.AppBar.TopBar

class ComposeActivity: BaseActivity() {

	override var toastContent: MutableState<ToastContent> = mutableStateOf(ToastContent())

	@Composable
	override fun Content() {
		val pageUrl = intent.getStringExtra("page")
		val page = Data.Pages[pageUrl]
		if(page == null) {
			Empty()
			return
		}

		Column {
			TopBar(
				title = page.name,
				navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
			){
				onBackPressedDispatcher.onBackPressed()
			}
			page.content(this@ComposeActivity)
		}
	}
	
	@Composable
	fun Empty(){
		Text(text = "This page is empty.")
	}
}