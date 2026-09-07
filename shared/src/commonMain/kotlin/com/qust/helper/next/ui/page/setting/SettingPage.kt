package com.qust.helper.next.ui.page.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.repository.SettingRepository
import com.qust.helper.next.ui.business.setting.InputItemUI
import com.qust.helper.next.ui.business.setting.SettingGroupUI
import com.qust.helper.next.ui.business.setting.SpinnerItemUI
import com.qust.helper.next.ui.business.setting.SwitchItemUI
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.ColumnVerticalScroll
import com.qust.helper.next.ui.component.FontScale
import com.qust.helper.next.ui.component.UiScale
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import com.qust.helper.next.utils.DateUtils
import io.github.composefluent.component.ExpanderItemSeparator


@Preview
@Composable
private fun SettingPreview() = AppPreview(::SettingUI)

class SettingPage : AppPage<SettingViewModel>(
	title = "设置",
	icon = Icons.Default.Settings,
	viewModelClass = SettingViewModel::class
) {
	@Composable
	override fun Content(viewModel: SettingViewModel) = SettingUI(viewModel)
}

@Composable
private fun SettingUI(viewModel: SettingViewModel) {
	ColumnVerticalScroll(
		modifier = Modifier.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		LessonTableSetting(viewModel)
		UISetting()
	}
}


/**
 * 课表设置界面
 */
@Composable
private fun LessonTableSetting(viewModel: SettingViewModel) {
	val setting by viewModel.lessonTableSetting.collectAsState()

	SettingGroupUI(
		title = "课表设置",
		description = "配置课表的展示样式"
	) {

		SwitchItemUI(
			title = "显示非本周课程",
			description = "是否将非本周课程以灰色显示",
			value = setting.showAllLesson
		) {
			LessonTableRepository.updateSetting(setting.copy(showAllLesson = it))
		}

		ExpanderItemSeparator()

		SwitchItemUI(
			title = "显示后续无课课程",
			description = "是否将后续周无课课程以灰色显示",
			value = setting.showFinishedLesson,
			enable = setting.showAllLesson
		) {
			LessonTableRepository.updateSetting(setting.copy(showFinishedLesson = it))
		}

		ExpanderItemSeparator()

		SwitchItemUI(
			title = "隐藏教师",
			description = "每周课表是否显示教师信息",
			value = setting.hideTeacher
		) {
			LessonTableRepository.updateSetting(setting.copy(hideTeacher = it))
		}

		ExpanderItemSeparator()

		InputItemUI(
			title = "设置开学时间",
			label = "开学时间",
			value = viewModel.startDay,
			onInput = viewModel::setStartDay
		)

		ExpanderItemSeparator()

		InputItemUI(
			title = "设置总周数",
			label = "总周数",
			value = viewModel.totalWeek.toString(),
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
			onInput = viewModel::setTotalWeek
		)
	}
}

@Composable
private fun UISetting(){
	val setting by SettingRepository.uiSetting.collectAsStateWithLifecycle()
	SettingGroupUI(
		title = "界面设置",
		description = "配置应用界面"
	) {
		SwitchItemUI(
			title = "百分比布局",
			description = "启用后，界面将根据窗口大小维持百分比布局，而不是固定布局",
			value = setting.percentageLayout,
			onChange = {
				SettingRepository.setUiSetting(SettingRepository.uiSetting.value.copy(percentageLayout = it))
			}
		)

		ExpanderItemSeparator()

		SpinnerItemUI(
			title = "界面大小",
			description = "调整应用界面大小",
			value = remember(setting) { UiScale.entries.find { it.scale == setting.uiScale }?.title ?: "" },
			items = UiScale.entries.map { it.title },
			onSelect = { index, item ->
				SettingRepository.setUiSetting(SettingRepository.uiSetting.value.copy(uiScale = UiScale.entries[index].scale))
			}
		)

		ExpanderItemSeparator()

		SpinnerItemUI(
			title = "字体大小",
			description = "调整应用字体大小",
			value = remember(setting) { FontScale.entries.find { it.scale == setting.fontScale }?.title ?: "" },
			items = FontScale.entries.map { it.title },
			onSelect = { index, item ->
				SettingRepository.setUiSetting(SettingRepository.uiSetting.value.copy(fontScale = FontScale.entries[index].scale))
			}
		)
	}
}


class SettingViewModel : BaseViewModel() {

	val _startDay = mutableStateOf(DateUtils.YMD.format(LessonTableRepository.currentLessonTable.value.startDay))
	val startDay by _startDay
	fun setStartDay(startDayStr: String){
		try {
			_startDay.value = startDayStr
			LessonTableRepository.updateStartDay(DateUtils.YMD.parse(startDayStr))
			toastSuccess("设置成功")
		} catch(e: Exception) {
			Logger.e("", e)
			toastWarning("请输入正确的日期")
		}
	}

	var totalWeek by mutableIntStateOf(LessonTableRepository.currentLessonTable.value.totalWeek)
	fun setTotalWeek(weekStr: String){
		val week = weekStr.toIntOrNull()
		if(week == null){
			toastWarning("请输入正确的数字")
			return
		}
		totalWeek = week
		LessonTableRepository.updateTotalWeek(week)
		toastSuccess("设置成功")
	}

	val lessonTableSetting = LessonTableRepository.setting

}