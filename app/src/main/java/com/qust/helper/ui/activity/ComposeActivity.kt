package com.qust.helper.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.qust.helper.data.Data
import com.qust.helper.ui.widget.AppWidgets.TopBar

class ComposeActivity: BaseActivity() {

	companion object{
		fun startActivity(context: Context, page: String){
			context.startActivity(Intent(context, ComposeActivity::class.java).putExtra("page", page))
		}
	}

	@Composable
	override fun Content() {
		val pageUrl = intent.getStringExtra("page")
		val page = Data.Pages[pageUrl]
		if(page == null) {
			Empty()
			return
		}

		val navController = rememberNavController()
		Scaffold(
			topBar = {
				TopBar(title = page.name, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack){
					onBackPressedDispatcher.onBackPressed()
				}
			}
		) { contentPadding ->
			page.content(this@ComposeActivity, contentPadding, navController, Bundle.EMPTY)
		}
	}
	
	@Composable
	fun Empty(){
		Text(text = "This page is empty.")
	}
}