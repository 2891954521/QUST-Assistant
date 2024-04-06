package com.qust.helper.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.ui.page.LoginPage
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.Dialogs
import com.qust.helper.ui.widget.ToastContent
import com.qust.helper.viewmodel.account.EasAccountViewModel
import kotlinx.coroutines.delay

class GuideActivity : BaseActivity() {

	private val viewModel: EasAccountViewModel by viewModels()

	override var toastContent: MutableState<ToastContent>
		get() = viewModel.toastContent
		set(_) {}

	@Composable
	override fun Content() {
		var animPlay by remember{ mutableStateOf(false) }
		val animationTransition = updateTransition(targetState = animPlay, label = "enterAnim")
		val y by animationTransition.animateDp(label = "offsetAnim") { if(it) 0.dp else 100.dp }
		val alpha by animationTransition.animateFloat(label = "alphaAnim") { if(it) 1f else 0.5f }
		if(!animPlay) {
			LaunchedEffect(Unit){
				delay(300L)
				animPlay = true
			}
		}

		Column(
			modifier = Modifier.statusBarsPadding().fillMaxSize().offset(y = y).alpha(alpha = alpha),
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			Row(
				modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 32.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.text_welcome),
					modifier = Modifier.padding(8.dp).weight(1f),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.primary,
					textAlign = TextAlign.Start,
				)
				TextButton(onClick = { btnSkip() }) {
					Text(
						text = stringResource(id = R.string.text_skip),
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText
					)
				}
			}

			Text(
				text = "\t欢迎使用青科助手，我们需要以下信息以便使用软件完整功能",
				modifier = Modifier.padding(16.dp),
				style = MaterialTheme.typography.bodyMedium,
			)

			LoginPage.EASLoginView(viewModel){
				loginSuccess()
			}
		}

		if(viewModel.dialogText.value.isNotEmpty()) Dialogs.IndeterminateProgressDialog(viewModel.dialogText.value)
	}

	private fun btnSkip() {
		Setting.edit { it.putBoolean(Keys.IS_FIRST_USE, false) }
		finish()
	}

	private fun loginSuccess(){
		Setting.edit { it.putBoolean(Keys.IS_FIRST_USE, false) }
		onBackPressedDispatcher.onBackPressed()
	}


//		open fun getLessonTable() {
//			SettingUtil.edit().putInt(getString(R.string.KEY_ENTRANCE_TIME), entranceTime).apply()
//			handler.sendMessage(handler.obtainMessage(HandlerCode.UPDATE_DIALOG, "正在查询课表"))
//			val index: Int = LessonTableModel.getCurrentYear(entranceTime)
//			try {
//				val result: QueryLessonResult = LessonTableModel.queryLessonTable(eaViewModel, (index / 2 + entranceTime).toString(), if(entranceTime % 2 == 0) "3" else "12")
//				LessonTableViewModel.getInstance(this).saveLessonData(result.lessonTable)
//			} catch(ignored: NeedLoginException) {
//			}
//			runOnUiThread {
//				SettingUtil.edit().putBoolean(getString(R.string.isFirstUse), false).apply()
//				dialog.dismiss()
//				toastOK("初始化完成")
//				startActivity(Intent(this@GuideActivity, MainActivity::class.java))
//				finish()
//			}
//		}
//	}

	@Preview(showBackground = true)
	@Composable
	fun GuideContentPreview() {
		AppTheme {
			Content()
		}
	}
}

