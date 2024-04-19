package com.qust.helper.ui.page.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qust.helper.data.api.QustApi
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.activity.ComposeActivity
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.Dialogs
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.SettingViewModel

object SettingPage {

	private val timeTableList = arrayOf("冬季 (13:30上课)", "夏季 (14:00上课)" )

	@Composable
	fun SettingPage(padding: PaddingValues, viewModel: SettingViewModel) {
		val context = LocalContext.current

		val scrollState = rememberScrollState()

		var startDayStr by remember { mutableStateOf(DateUtils.YMD.format(LessonTableRepository.startDay)) }
		var showTimePicker by remember { mutableStateOf(false) }

		Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)) {

			SettingGroup("课表") {

				SwitchItem("显示全部课程", "非本周课程会以灰色显示", "非本周课程不会显示", LessonTableRepository.showAllLesson) { LessonTableRepository.setShowAllLessonValue(it) }

				SwitchItem("隐藏已结课程", "已结课程不会出现在课表中", "已结课程会出现在课表中", LessonTableRepository.hideFinishLesson) { LessonTableRepository.setHideFinishLessonValue(it) }

				SwitchItem("隐藏教师", "每周课表不会显示教师信息", "每周课表会显示教师信息", LessonTableRepository.hideTeacher) { LessonTableRepository.setHideTeacherValue(it) }

				SwitchItem("锁定课表", "不允许编辑课表", "允许编辑课表", viewModel.lockLesson) { viewModel.lockLesson = it }

				SettingItem("设置开学时间", startDayStr){ showTimePicker = true }

				InputItem("设置总周数", LessonTableRepository.totalWeek.toString(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
				){ week -> week.toIntOrNull()?.let{ LessonTableRepository.setTotalWeekValue(it) } }

				ListItem("设置时间表", LessonTableRepository.currentTimeTable, timeTableList){ _, it -> LessonTableRepository.setTimeTableValue(it) }

				InputItem("设置入学年份", viewModel.entranceTime,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
				){ year -> year.toIntOrNull()?.let{ viewModel.setEntranceTimeValue(it) }  }
			}

			SettingGroup("教务") {
				ListItem("教务节点", viewModel.eaHost, QustApi.EA_HOSTS){ _, it -> viewModel.setEaHostValue(it) }
			}

			SettingGroup("界面") {
				SwitchItem("主题跟随系统", "主题跟随系统", "主题跟随系统", viewModel.themeFollowSystem){ viewModel.setThemeFollowSystemValue(it) }
				if(viewModel.themeFollowSystem){
					Box(modifier = Modifier.fillMaxWidth()) {
						Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
							Column(modifier = Modifier.weight(1F)) {
								Text(text = "暗色模式", style = MaterialTheme.typography.titleMedium, color = colorSecondaryText)
								Text(text = if(viewModel.themeDark) "暗色模式已开启" else "暗色模式已关闭", color = colorSecondaryText)
							}
							Switch(checked = viewModel.themeDark, enabled = false, onCheckedChange = { })
						}
					}
				}else{
					SwitchItem("暗色模式", "暗色模式已开启", "暗色模式已关闭", viewModel.themeDark){ viewModel.setThemeDarkValue(it) }
				}
			}

			SettingGroup("更新") {
				SwitchItem("自动检查更新", "3天检查一次更新", "不检查更新", viewModel.autoUpdate){ viewModel.setAutoUpdateValue(it) }
				SettingItem("检查更新"){ ComposeActivity.startActivity(context, "update") }
			}

			SettingGroup("其他") {
				SettingItem("应用版本", viewModel.appVersion)
				SettingItem("构建时间", viewModel.buildTime)

				val uriHandler = LocalUriHandler.current
				SettingItem("源代码", "GitHub"){
					uriHandler.openUri("https://github.com/2891954521/QUST-Assistant")
				}
				SettingItem("用户许可协议", ""){
					ComposeActivity.startActivity(context, "userAgreement")
				}
				SettingItem("隐私政策", ""){
					ComposeActivity.startActivity(context, "policy")
				}
			}
		}

		if(showTimePicker){
			Dialogs.DatePickerDialog(currentDate = LessonTableRepository.startDay, onDismissRequest = { showTimePicker = false }){
				LessonTableRepository.setStartDayValue(it)
				startDayStr = DateUtils.YMD.format(it)
				showTimePicker = false
			}
		}
	}

	@Composable
	fun SettingGroup(title: String, content: @Composable () -> Unit) {
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
	fun SettingItem(title: String, description: String = "", onClick: () -> Unit = { }) {
		Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium)
				if(description.isNotEmpty()) Text(text = description, color = colorSecondaryText)
			}
		}
	}

	@Composable
	fun SwitchItem(
		title: String,
		onText: String = "",
		offText: String = "",
		initValue: Boolean = false,
		onChange: (Boolean) -> Unit = { }
	) {
		var switchValue by remember { mutableStateOf(initValue) }
		Box(modifier = Modifier.fillMaxWidth().clickable {  }) {
			Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Column(modifier = Modifier.weight(1F)) {
					Text(text = title, style = MaterialTheme.typography.titleMedium)
					Text(text = if(switchValue) onText else offText, color = colorSecondaryText)
				}
				Switch(
					modifier = Modifier.semantics { contentDescription = "Demo" },
					checked = switchValue,
					onCheckedChange = { switchValue = it; onChange(it) }
				)
			}
		}
	}

	@Composable
	fun InputItem(title: String, value: String = "", keyboardOptions:KeyboardOptions = KeyboardOptions.Default, onInput: (String) -> Unit = { }) {
		var showInput by remember { mutableStateOf(false) }
		Box(modifier = Modifier.fillMaxWidth().clickable { showInput = true }){
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium)
				Text(text = value, color = colorSecondaryText)
			}
		}
		if(showInput){
			Dialogs.InputDialog(
				title = title,
				content = value,
				keyboardOptions = keyboardOptions,
				onDismiss = { showInput = false }
			){
				onInput(it)
				showInput = false
			}
		}
	}

	@Composable
	fun ListItem(title: String, index: Int, items: Array<String>, onSelect: (Array<String>, Int) -> Unit = { _, _ -> }) {
		var showList by remember { mutableStateOf(false) }
		Box(modifier = Modifier.fillMaxWidth().clickable { showList = true }){
			Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
				Text(text = title, style = MaterialTheme.typography.titleMedium)
				Text(text = items[index], color = colorSecondaryText)
			}
		}
		if(showList){
			Dialogs.ListDialog(
				title = title,
				items,
				onDismiss = { showList = false }
			){ item, it ->
				onSelect(item, it)
				showList = false
			}
		}
	}
}