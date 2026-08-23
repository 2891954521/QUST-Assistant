package com.qust.helper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qust.helper.data.Keys
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.utils.SettingUtils

class MainActivity: ComponentActivity() {

	val page = HomePage

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// 第一次使用跳转到引导页（用户协议）
		if(SettingUtils[Keys.IS_FIRST_USE, true]) {
			startActivity(Intent(this, GuideActivity::class.java))
		}

		setContent { AppTheme { HomePage.ComposePage() } }
	}
}
