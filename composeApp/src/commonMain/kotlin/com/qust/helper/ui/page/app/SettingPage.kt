package com.qust.helper.ui.page.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.QustApi
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.widget.layout.InputItemUI
import com.qust.helper.ui.widget.layout.ListItemUI
import com.qust.helper.ui.widget.layout.SettingGroupUI
import com.qust.helper.ui.widget.layout.SettingItemUI
import com.qust.helper.ui.widget.layout.SwitchItemUI
import com.qust.helper.viewmodel.app.SettingViewModel

object SettingPage: BasePage<SettingViewModel>("设置", Icons.Default.Settings) {

	@Composable
	override fun getViewModel() = viewModel<SettingViewModel>()


	@Composable
	override fun Content(viewModel: SettingViewModel) {
		val pageController = com.qust.helper.ui.page.rememberPageController()
		Column(Modifier.fillMaxSize()) {
			AllSetting(viewModel, pageController)
		}
	}



	@Composable
	fun AllSetting(viewModel: SettingViewModel, pageController: com.qust.helper.ui.page.PageController) {
		LessonTableSetting(viewModel)

		EasSetting(viewModel)

		SettingGroupUI("界面") {
			SwitchItemUI("主题跟随系统", "主题跟随系统", "主题跟随系统", viewModel.themeFollowSystem){ viewModel.setThemeFollowSystemValue(it) }
			SwitchItemUI("暗色模式", "暗色模式已开启", "暗色模式已关闭", viewModel.themeDark, enable = !viewModel.themeFollowSystem){ viewModel.setThemeDarkValue(it) }
		}

		SettingGroupUI("更新") {
			SwitchItemUI("自动检查更新", "3天检查一次更新", "不检查更新", viewModel.autoUpdate){ viewModel.setAutoUpdateValue(it) }
			SettingItemUI("检查更新", ""){
				pageController.startPage(UpdatePage)
			}
		}

		SettingGroupUI("其他") {
			val uriHandler = LocalUriHandler.current
			SettingItemUI("源代码", "GitHub"){
				uriHandler.openUri("https://github.com/2891954521/QUST-Assistant")
			}
			SettingItemUI("用户许可协议", ""){
				pageController.startPage(com.qust.helper.ui.page.app.AppPage.UserAgreementPage)
			}
			SettingItemUI("隐私政策", ""){
				pageController.startPage(com.qust.helper.ui.page.app.AppPage.PolicyPage)
			}
		}
	}

	/**
	 * 课表设置界面
	 */
	@Composable
	fun LessonTableSetting(viewModel: SettingViewModel) {
		SettingGroupUI("课表") {

			SwitchItemUI("显示非本周课程", "是否将非本周课程以灰色显示", value = LessonTableRepository.showAllLesson) { LessonTableRepository.setShowAllLessonValue(it) }

			SwitchItemUI("隐藏后续无课课程", "如果一节课后续无课则不会以灰色显示", value = LessonTableRepository.hideFinishLesson, enable = LessonTableRepository.showAllLesson) { LessonTableRepository.setHideFinishLessonValue(it) }

			SwitchItemUI("隐藏教师", "每周课表是否显示教师信息", value = LessonTableRepository.hideTeacher) { LessonTableRepository.setHideTeacherValue(it) }

			SwitchItemUI("锁定课表", "不允许编辑课表", "允许编辑课表", LessonTableRepository.lockLesson) { LessonTableRepository.setLockLessonValue(it)}

			InputItemUI(
				title = "设置开学时间",
				value = viewModel.startDay,
				onInput = viewModel::setStartDay
			)

			InputItemUI(
				title = "设置总周数",
				value = viewModel.totalWeek.toString(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
				onInput = viewModel::setTotalWeek
			)
		}
	}


	/**
	 * 教务查询设置
	 */
	@Composable
	fun EasSetting(viewModel: SettingViewModel) {
		SettingGroupUI("教务") {
			ListItemUI("教务节点", viewModel.eaHost, QustApi.EA_HOSTS){ _, it -> viewModel.setEaHostValue(it) }

			InputItemUI("设置入学年份", viewModel.entranceTime.value, KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)){
					year -> year.toIntOrNull()?.let{ viewModel.setEntranceTime(it) }
			}

			SwitchItemUI("使用VPN访问教务系统", "使用智慧青科大的VPN访问教务系统，可以解决校外访问教务失败的问题。会减慢查询速度，请仅在需要时打开。重启应用生效", value = viewModel.eaUseVpn) {
				viewModel.setEaUseVpnValue(it)
			}
		}
	}
}