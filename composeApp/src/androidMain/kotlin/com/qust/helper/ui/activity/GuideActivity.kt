package com.qust.helper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.qust.helper.data.Keys
import com.qust.helper.ui.page.app.AppPage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.utils.SettingUtils

/**
 * 首次开屏 - 用户协议同意页
 */
class GuideActivity: ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			AppTheme {
				GuidePage(
					onAgree = {
						SettingUtils[Keys.IS_FIRST_USE] = false
						finish()
					}
				)
			}
		}
	}

	@Composable
	fun GuidePage(onAgree: () -> Unit) {
		var checked by remember { mutableStateOf(false) }
		val context = LocalContext.current
		val pageController = rememberPageController()

		Column(
			modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = "欢迎使用青科助手",
				modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.primary,
				textAlign = TextAlign.Center
			)

			Text(
				text = "青科助手是青岛科技大学在校学生开发的校园服务应用。在使用本应用前，请您仔细阅读并同意以下条款：",
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.padding(vertical = 8.dp)
			)

			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Checkbox(checked = checked, onCheckedChange = { checked = it })
				val str = buildAnnotatedString {
					append("我已阅读并同意 ")
					pushStringAnnotation(tag = "userAgreement", annotation = "《用户协议》")
					withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) { append("《用户协议》") }
					pop()
					append(" 和 ")
					pushStringAnnotation(tag = "policy", annotation = "《隐私政策》")
					withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) { append("《隐私政策》") }
					pop()
				}
				ClickableText(text = str, onClick = { offset ->
					str.getStringAnnotations(tag = "userAgreement", start = offset, end = offset).firstOrNull()?.let {
						pageController.startPage(AppPage.UserAgreementPage)
					}
					str.getStringAnnotations(tag = "policy", start = offset, end = offset).firstOrNull()?.let {
						pageController.startPage(AppPage.PolicyPage)
					}
				})
			}

			Button(
				modifier = Modifier.fillMaxWidth().padding(16.dp),
				onClick = { if(checked) onAgree() }
			) {
				Text(text = "同意并开始使用")
			}

			TextButton(
				modifier = Modifier.fillMaxWidth(),
				onClick = { finish() }
			) {
				Text(text = "不同意并退出")
			}
		}
	}
}
