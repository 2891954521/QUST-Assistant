package com.qust.helper.ui.activity

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.page.GuidePage
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.viewmodel.account.EasAccountViewModel

class GuideActivity : BaseActivity() {

	private val viewModel: EasAccountViewModel by viewModels()

	override var toastContent: MutableState<ToastContent>
		get() = viewModel.toastContent
		set(_) {}

	@Composable
	override fun Content() {

		GuidePage.EasAccountLogin(
			viewModel = viewModel,
			dialogText = viewModel.dialogText.value,
			toastContent = viewModel.toastContent,
			btnSkip = { btnSkip() },
			loginSuccess = { loginSuccess() }
		)
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

