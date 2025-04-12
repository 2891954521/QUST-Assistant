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
import com.qust.helper.model.SettingModel
import com.qust.helper.model.lessonTable.LessonTableModel
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.widget.layout.InputItemUI
import com.qust.helper.ui.widget.layout.SettingGroupUI
import com.qust.helper.ui.widget.layout.SettingItemUI
import com.qust.helper.viewmodel.app.SettingViewModel

object SettingPage: BasePage<SettingViewModel>("设置", Icons.Default.Settings) {

	@Composable
	override fun getViewModel() = viewModel<SettingViewModel>()


	@Composable
	override fun Content(viewModel: SettingViewModel) {
		Column(Modifier.fillMaxSize()) {
			AllSetting(viewModel)
		}
	}



	@Composable
	fun AllSetting(viewModel: SettingViewModel) {
//		val context = LocalContext.current

		LessonTableSetting(viewModel)

//		EasSetting(viewModel = viewModel)

//		SettingGroupUI("界面") {
//			SwitchItemUI("主题跟随系统", "主题跟随系统", "主题跟随系统", viewModel.themeFollowSystem){ viewModel.setThemeFollowSystemValue(it) }
//			SwitchItemUI("暗色模式", "暗色模式已开启", "暗色模式已关闭", viewModel.themeDark, enable = !viewModel.themeFollowSystem){ viewModel.setThemeDarkValue(it) }
//		}

//		SettingGroupUI("更新") {
//			SwitchItemUI("自动检查更新", "3天检查一次更新", "不检查更新", viewModel.autoUpdate){ viewModel.setAutoUpdateValue(it) }
//			SettingItemUI("检查更新", ""){ ComposeActivity.startActivity(context, Keys.Page.UpdatePage) }
//		}

		SettingGroupUI("其他") {
//			SettingItemUI("应用版本", viewModel.appVersion){ }
//			SettingItemUI("构建时间", viewModel.buildTime){ }

			val uriHandler = LocalUriHandler.current
			SettingItemUI("源代码", "GitHub"){
				uriHandler.openUri("https://github.com/2891954521/QUST-Assistant")
			}
			SettingItemUI("用户许可协议", ""){
//				ComposeActivity.startActivity(context, Keys.Page.UserAgreementPage)
			}
			SettingItemUI("隐私政策", ""){
//				ComposeActivity.startActivity(context, Keys.Page.PolicyPage)
			}
		}
	}

	/**
	 * 课表设置界面
	 */
	@Composable
	fun LessonTableSetting(viewModel: SettingViewModel) {
//		var showTimePicker by remember { mutableStateOf(false) }

//		var startDayStr by remember { mutableStateOf(DateUtils.YMD.format(LessonTableRepository.startDay)) }

		SettingGroupUI("课表") {

//			SwitchItemUI("显示非本周课程", "是否将非本周课程以灰色显示", value = LessonTableRepository.showAllLesson) { LessonTableRepository.setShowAllLessonValue(it) }
//
//			SwitchItemUI("隐藏后续无课课程", "如果一节课后续无课则不会以灰色显示", value = LessonTableRepository.hideFinishLesson, enable = LessonTableRepository.showAllLesson) { LessonTableRepository.setHideFinishLessonValue(it) }
//
//			SwitchItemUI("隐藏教师", "每周课表是否显示教师信息", value = LessonTableRepository.hideTeacher) { LessonTableRepository.setHideTeacherValue(it) }
//
//			SwitchItemUI("锁定课表", "不允许编辑课表", "允许编辑课表", LessonTableRepository.lockLesson) { LessonTableRepository.setLockLessonValue(it)}

//			SettingItemUI("设置开学时间", startDayStr){ showTimePicker = true }

			InputItemUI("设置总周数", value = viewModel.totalWeek.toString(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
			){ week ->
				week.toIntOrNull()?.let {
					LessonTableModel._totalWeek.value = it
					SettingModel.totalWeek = it
				}
			}

//			SwitchItemUI("使用高密时间表（重启app生效）", "使用高密时间表", "使用默认时间表",value = LessonTableRepository.gaomiTimeTable) { LessonTableRepository.setGaomiTimeTable(it) }


//			ListItemUI("设置时间表\n（重启app生效）", LessonTableRepository.currentTimeTable, timeTableList){ _, it -> LessonTableRepository.setTimeTableValue(it) }
		}

//		if(showTimePicker){
//			DatePickerDialog(currentDate = LessonTableRepository.startDay, onDismissRequest = { showTimePicker = false }){
//				LessonTableRepository.setStartDayValue(it)
//				startDayStr = DateUtils.YMD.format(it)
//				showTimePicker = false
//			}
//		}
	}


	/**
	 * 教务查询设置
	 */
//	@Composable
//	fun EasSetting(viewModel: SettingViewModel) {
//		SettingGroupUI("教务") {
//			ListItemUI("教务节点", viewModel.eaHost, QustApi.EA_HOSTS){ _, it -> viewModel.setEaHostValue(it) }
//
//			InputItemUI("设置入学年份", viewModel.entranceTime, KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)){
//					year -> year.toIntOrNull()?.let{ viewModel.setEntranceTimeValue(it) }
//			}
//
//			SwitchItemUI("使用VPN访问教务系统", "使用智慧青科大的VPN访问教务系统，可以解决校外访问教务失败的问题。会减慢查询速度，请仅在需要时打开。重启应用生效", value = viewModel.eaUseVpn) {
//				viewModel.setEaUseVpnValue(it)
//			}
//		}
//	}
}