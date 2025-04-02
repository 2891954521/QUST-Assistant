package com.qust.helper.ui.page.app

import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.data.QustApi
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.activity.ComposeActivity
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.DatePickerDialog
import com.qust.helper.ui.widget.InputDialog
import com.qust.helper.ui.widget.ListDialog
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.SettingViewModel

object SettingPage {

	const val LessonTable = "LessonTableSetting"
	const val Eas = "EasSetting"
	const val App = "AppSetting"

	val SettingPage = Page(
		Keys.Page.SettingPage, "设置", image = Icons.Rounded.Settings,
		arguments = listOf(navArgument("type") { type = NavType.StringType; nullable = true })
	) { activity, padding, _, arguments ->
		val scrollState = rememberScrollState()

		Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)) {
			when(arguments?.getString("type")){
				LessonTable -> LessonTableSetting()
				Eas -> {
					val viewModel by activity.viewModels<SettingViewModel>()
					EasSetting(viewModel = viewModel)
				}
				null -> {
					val viewModel by activity.viewModels<SettingViewModel>()
					AllSetting(viewModel)
				}
			}
		}
	}

	private val timeTableList = arrayOf("冬季 (13:30上课)", "夏季 (14:00上课)" )


	@Composable
	fun AllSetting(viewModel: SettingViewModel) {
		val context = LocalContext.current

		LessonTableSetting()

		EasSetting(viewModel = viewModel)

		SettingGroupUI("界面") {
			SwitchItemUI("主题跟随系统", "主题跟随系统", "主题跟随系统", viewModel.themeFollowSystem){ viewModel.setThemeFollowSystemValue(it) }
			SwitchItemUI("暗色模式", "暗色模式已开启", "暗色模式已关闭", viewModel.themeDark, enable = !viewModel.themeFollowSystem){ viewModel.setThemeDarkValue(it) }
		}

		SettingGroupUI("更新") {
			SwitchItemUI("自动检查更新", "3天检查一次更新", "不检查更新", viewModel.autoUpdate){ viewModel.setAutoUpdateValue(it) }
			SettingItemUI("检查更新", ""){ ComposeActivity.startActivity(context, Keys.Page.UpdatePage) }
		}

		SettingGroupUI("其他") {
			SettingItemUI("应用版本", viewModel.appVersion){ }
			SettingItemUI("构建时间", viewModel.buildTime){ }

			val uriHandler = LocalUriHandler.current
			SettingItemUI("源代码", "GitHub"){
				uriHandler.openUri("https://github.com/2891954521/QUST-Assistant")
			}
			SettingItemUI("用户许可协议", ""){
				ComposeActivity.startActivity(context, Keys.Page.UserAgreementPage)
			}
			SettingItemUI("隐私政策", ""){
				ComposeActivity.startActivity(context, Keys.Page.PolicyPage)
			}
		}
	}

	/**
	 * 课表设置界面
	 */
	@Composable
	fun LessonTableSetting() {
		var showTimePicker by remember { mutableStateOf(false) }

		var startDayStr by remember { mutableStateOf(DateUtils.YMD.format(LessonTableRepository.startDay)) }

		SettingGroupUI("课表") {

			SwitchItemUI("显示非本周课程", "是否将非本周课程以灰色显示", value = LessonTableRepository.showAllLesson) { LessonTableRepository.setShowAllLessonValue(it) }

			SwitchItemUI("隐藏后续无课课程", "如果一节课后续无课则不会以灰色显示", value = LessonTableRepository.hideFinishLesson, enable = LessonTableRepository.showAllLesson) { LessonTableRepository.setHideFinishLessonValue(it) }

			SwitchItemUI("隐藏教师", "每周课表是否显示教师信息", value = LessonTableRepository.hideTeacher) { LessonTableRepository.setHideTeacherValue(it) }

			SwitchItemUI("锁定课表", "不允许编辑课表", "允许编辑课表", LessonTableRepository.lockLesson) { LessonTableRepository.setLockLessonValue(it)}

			SettingItemUI("设置开学时间", startDayStr){ showTimePicker = true }

			InputItemUI("设置总周数", LessonTableRepository.totalWeek.toString(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
			){ week -> week.toIntOrNull()?.let{ LessonTableRepository.setTotalWeekValue(it) } }

			SwitchItemUI("使用高密时间表（重启app生效）", "使用高密时间表", "使用默认时间表",value = LessonTableRepository.gaomiTimeTable) { LessonTableRepository.setGaomiTimeTable(it) }


			ListItemUI("设置时间表\n（重启app生效）", LessonTableRepository.currentTimeTable, timeTableList){ _, it -> LessonTableRepository.setTimeTableValue(it) }
		}

		if(showTimePicker){
			DatePickerDialog(currentDate = LessonTableRepository.startDay, onDismissRequest = { showTimePicker = false }){
				LessonTableRepository.setStartDayValue(it)
				startDayStr = DateUtils.YMD.format(it)
				showTimePicker = false
			}
		}
	}


	/**
	 * 教务查询设置
	 */
	@Composable
	fun EasSetting(viewModel: SettingViewModel) {
		SettingGroupUI("教务") {
			ListItemUI("教务节点", viewModel.eaHost, QustApi.EA_HOSTS){ _, it -> viewModel.setEaHostValue(it) }

			InputItemUI("设置入学年份", viewModel.entranceTime, KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)){
				year -> year.toIntOrNull()?.let{ viewModel.setEntranceTimeValue(it) }
			}

			SwitchItemUI("使用VPN访问教务系统", "使用智慧青科大的VPN访问教务系统，可以解决校外访问教务失败的问题。会减慢查询速度，请仅在需要时打开。重启应用生效", value = viewModel.eaUseVpn) {
				viewModel.setEaUseVpnValue(it)
			}
		}
	}

	@Composable
	private fun SettingGroupUI(title: String, content: @Composable () -> Unit) {
		Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)) {
			Text(
				text = title,
				modifier = Modifier.padding(start = 16.dp, top = 16.dp),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary
			)
			content()
		}
	}

	@Composable
	private fun SettingItemUI(title: String, description: String, enable: Boolean = true, onClick: () -> Unit) {
		Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else colorSecondaryText)
				if(description.isNotEmpty()) Text(text = description, color = colorSecondaryText)
			}
		}
	}

	@Composable
	private fun SwitchItemUI(title: String, onText: String, offText: String? = null, value: Boolean, enable: Boolean = true, onChange: (Boolean) -> Unit) {
		Box(modifier = Modifier.fillMaxWidth().clickable {  }) {
			Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Column(modifier = Modifier.weight(1F)) {
					Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else colorSecondaryText)
					Text(text = if(value) onText else offText ?: onText, color = colorSecondaryText)
				}
				Switch(
					modifier = Modifier.semantics { contentDescription = title }.padding(start = 8.dp),
					checked = value,
					enabled = enable,
					onCheckedChange = { onChange(it) }
				)
			}
		}
	}

	@Composable
	private fun InputItemUI(title: String, value: String, keyboardOptions: KeyboardOptions, enable: Boolean = true, onInput: (String) -> Unit) {
		var showInput by remember { mutableStateOf(false) }
		Box(modifier = Modifier.fillMaxWidth().clickable { showInput = true }){
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else colorSecondaryText)
				Text(text = value, color = colorSecondaryText)
			}
		}
		if(showInput){
			InputDialog(title = title, content = value, keyboardOptions = keyboardOptions, onDismiss = { showInput = false }){
				onInput(it)
				showInput = false
			}
		}
	}

	@Composable
	private fun ListItemUI(title: String, index: Int, items: Array<String>, enable: Boolean = true, onSelect: (Array<String>, Int) -> Unit) {
		var showList by remember { mutableStateOf(false) }
		Box(modifier = Modifier.fillMaxWidth().clickable { showList = true }){
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium, color = if(enable) Color.Unspecified else colorSecondaryText)
				Text(text = items[index], color = colorSecondaryText)
			}
		}
		if(showList){
			ListDialog(title = title, items, onDismiss = { showList = false }){ item, it ->
				onSelect(item, it)
				showList = false
			}
		}
	}

}