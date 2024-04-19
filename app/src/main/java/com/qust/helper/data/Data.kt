package com.qust.helper.data

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.qust.helper.R
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.model.account.EASAccount
import com.qust.helper.ui.activity.BaseActivity
import com.qust.helper.ui.page.DrinkPage
import com.qust.helper.ui.page.ElectricRecharge
import com.qust.helper.ui.page.LoginPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.app.UpdatePage
import com.qust.helper.ui.page.eas.GetAcademic
import com.qust.helper.ui.page.eas.GetExams
import com.qust.helper.ui.page.eas.GetLessonTable
import com.qust.helper.ui.page.eas.GetMarks
import com.qust.helper.ui.page.eas.GetNotice
import com.qust.helper.ui.page.lesson.DailyLesson
import com.qust.helper.ui.page.lesson.TermLesson
import com.qust.helper.ui.page.web.WebPage
import com.qust.helper.viewmodel.DrinkViewModel
import com.qust.helper.viewmodel.ElectricRechargeViewModel
import com.qust.helper.viewmodel.SettingViewModel
import com.qust.helper.viewmodel.TermLessonViewModel
import com.qust.helper.viewmodel.account.EasAccountViewModel
import com.qust.helper.viewmodel.account.IPassAccountViewModel
import com.qust.helper.viewmodel.eas.GetAcademicViewModel
import com.qust.helper.viewmodel.eas.GetExamsViewModel
import com.qust.helper.viewmodel.eas.GetLessonTableViewModel
import com.qust.helper.viewmodel.eas.GetMarksViewModel
import com.qust.helper.viewmodel.eas.GetNoticeViewModel

object Data {

	val Pages = mapOf(
		"dailyLesson" to Page("dailyLesson", "当日课表", iconRes = R.drawable.ic_article) { _, padding, _ ->
			DailyLesson.DailyLessonUI(
				padding = padding,
				lessonGroups = LessonTableRepository.lessonTable.lessons[LessonTableRepository.dayOfWeek.intValue],
				currentWeek = LessonTableRepository.currentWeek.intValue
			)
		},

		"termLesson" to Page("termLesson", "学期课表", iconRes = R.drawable.ic_grid_view) { activity, padding, _ ->
			val viewModel by activity.viewModels<TermLessonViewModel>()
			TermLesson.TermLessonUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent, toast = activity.toast)
		},

		"easLogin" to Page("easLogin", "教务登陆", iconRes = R.drawable.ic_login) { activity, padding, _ ->
			val viewModel by activity.viewModels<EasAccountViewModel>()
			LoginPage.EASLoginPage(padding, viewModel, activity.toast){
				activity.onBackPressedDispatcher.onBackPressed()
			}
		},

		"easWebPage" to Page("easWebPage", "教务系统", iconRes = R.drawable.ic_school, enableDrawer = false) { activity, padding, navController ->
			val viewModel by activity.viewModels<WebPage.WebPageViewModel>(factoryProducer = {
				object : ViewModelProvider.Factory {
					override fun <T : ViewModel> create(modelClass: Class<T>): T {
						return if (modelClass.isAssignableFrom(WebPage.WebPageViewModel::class.java)) {
							val account = EASAccount.getInstance()
							WebPage.WebPageViewModel(activity.application, account, "${account.scheme}://${account.host}/jwglxt/xtgl/index_initMenu.html") as T
						} else throw IllegalArgumentException("Unknown ViewModel class")
					}
				}
			})
			Box(modifier = Modifier.padding(padding)){
				WebPage.WebScreen(viewModel = viewModel, mainController = navController, toast = activity.toast)
			}
		},

		"vpnLogin" to Page("vpnLogin", "智慧青科大登陆", iconRes = R.drawable.ic_login) { activity, padding, _ ->
			val viewModel by activity.viewModels<IPassAccountViewModel>()
			LoginPage.IPassLoginPage(padding, viewModel, activity.toast){
				activity.onBackPressedDispatcher.onBackPressed()
			}
		},

		"easNotice" to Page("easNotice", "教务通知", iconRes = R.drawable.ic_notification) { activity, padding, navController ->
			val viewModel by activity.viewModels<GetNoticeViewModel>()
			GetNotice.GetNotice(padding, viewModel, activity.toast, navController)
		},

		"getLesson" to Page("getLesson", "课表查询", iconRes = R.drawable.ic_grid_view) { activity, padding, navController ->
			val viewModel by activity.viewModels<GetLessonTableViewModel>()
			GetLessonTable.GetLessonTable(padding, viewModel, activity.toast, navController)
		},
		"getMarks" to Page("getMarks", "成绩查询", iconRes = R.drawable.ic_article) { activity, padding, navController ->
			val viewModel by activity.viewModels<GetMarksViewModel>()
			GetMarks.GetMarksUI(padding, viewModel, activity.toast, navController)
		},
		"getAcademic" to Page("getAcademic", "学业查询", iconRes = R.drawable.ic_school) { activity, padding, navController ->
			val viewModel by activity.viewModels<GetAcademicViewModel>()
			GetAcademic.GetAcademic(padding, viewModel, activity.toast, navController)
		},
		"getExams" to Page("getExams", "考试查询", iconRes = R.drawable.ic_insert_invitation) { activity, padding, navController ->
			val viewModel by activity.viewModels<GetExamsViewModel>()
			GetExams.GetExamsUI(padding, viewModel, activity.toast, navController)
		},

		"drinkCode" to Page("drinkCode", "饮水码", iconRes = R.drawable.ic_water) { activity, padding, _ ->
			val viewModel by activity.viewModels<DrinkViewModel>()
			DrinkPage.DrinkPage(padding, viewModel, activity)
		},

		"electricRecharge" to Page("electricRecharge", "电费充值", iconRes = R.drawable.ic_electric) { activity, padding, navController ->
			val viewModel by activity.viewModels<ElectricRechargeViewModel>()
			ElectricRecharge.ElectricRecharge(padding, viewModel, activity.toast, navController)
		},

		"setting" to Page("setting", "设置", image = Icons.Rounded.Settings) { activity, padding, _ ->
			val viewModel by activity.viewModels<SettingViewModel>()
			SettingPage.SettingPage(padding, viewModel)
		},

		"update" to Page("update", "检查更新", hasEntrance = false) { activity, padding, _ ->
			val viewModel by activity.viewModels<UpdatePage.UpdateViewModel>()
			UpdatePage.UpdatePageUI(padding, viewModel, activity.toast)
		},

		"userAgreement" to Page("userAgreement", "用户协议", hasEntrance = false) { _, padding, _ ->
			Box(modifier = Modifier.padding(padding)){
				AndroidView(
					factory = { context ->
						val webView = WebView(context)
						webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
						webView.setBackgroundColor(Color.TRANSPARENT)
						webView.isVerticalScrollBarEnabled = true
						webView.settings.also { webSettings ->
							webSettings.allowFileAccess = false
							webSettings.loadWithOverviewMode = true
							webSettings.setSupportZoom(true)
							webSettings.displayZoomControls = true
						}
						webView.loadUrl("file:///android_asset/userAgreement.html")
						webView
					}
				)
			}
		},

		"policy" to Page("policy", "隐私政策", hasEntrance = false) { _, padding, _ ->
			Box(modifier = Modifier.padding(padding)){
				AndroidView(
					factory = { context ->
						val webView = WebView(context)
						webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
						webView.setBackgroundColor(Color.TRANSPARENT)
						webView.isVerticalScrollBarEnabled = true
						webView.settings.also { webSettings ->
							webSettings.allowFileAccess = false
							webSettings.loadWithOverviewMode = true
							webSettings.setSupportZoom(true)
							webSettings.displayZoomControls = true
						}
						webView.loadUrl("file:///android_asset/policy.html")
						webView
					}
				)
			}
		},
	)

	val TermName = arrayOf(
		"大一 上学期", "大一 下学期",
		"大二 上学期", "大二 下学期",
		"大三 上学期", "大三 下学期",
		"大四 上学期", "大四 下学期"
	)

	val WEEK_STRING = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

	/**
	 * 课程时间差 (单位：分钟)
	 */
	val LESSON_TIME = arrayOf(
		intArrayOf(0, 60, 70, 60, 140, 60, 70, 60, 80, 60),
		intArrayOf(0, 60, 70, 60, 170, 60, 70, 60, 80, 60)
	)


	class Page(
		val key: String,
		val name: String,
		val iconRes: Int = 0,
		val image: ImageVector? = null,
		val hasEntrance: Boolean = true,
		val enableDrawer: Boolean = true,
		val content: @Composable (BaseActivity, PaddingValues, NavController) -> Unit = { _, _, _ -> }
	)
}

