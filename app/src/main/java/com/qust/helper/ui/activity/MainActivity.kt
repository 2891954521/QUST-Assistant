package com.qust.helper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.ui.page.MainApp
import com.qust.helper.ui.widget.ToastContent

class MainActivity : BaseActivity() {

	override var toastContent: MutableState<ToastContent> = mutableStateOf(ToastContent())

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

