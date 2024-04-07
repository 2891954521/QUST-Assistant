package com.qust.helper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.ui.page.MainApp

class MainActivity : BaseActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// 第一次使用跳转到引导页
		if(Setting.getBoolean(Keys.IS_FIRST_USE, true)) {
			startActivity(Intent(this, GuideActivity::class.java))
			return
		}
	}

	@Composable
	override fun Content() {
		MainApp(this)
	}
}

