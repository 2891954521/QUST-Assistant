package com.qust.helper.next.ui.business.lesson.timeTableEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.entity.lesson.TimeTableCatalog
import com.qust.helper.next.entity.lesson.TimeTableConfig
import com.qust.helper.next.repository.TimeTableRepository
import com.qust.helper.next.ui.component.ButtonType
import com.qust.helper.next.ui.component.ColumnVerticalScroll
import com.qust.helper.next.ui.component.ConfirmDialog
import com.qust.helper.next.ui.component.PrimaryButton
import com.qust.helper.next.ui.component.button.TextButton
import com.qust.helper.next.ui.component.button.TextButtonColors
import com.qust.helper.next.ui.component.dialogs.InputDialog
import com.qust.helper.next.ui.component.input.TimePick
import com.qust.helper.next.ui.theme.Theme

/**
 * 时间表设置弹窗的交互接口
 *
 * 由 [TimeTableEditController] 实现
 */
interface TimeTableEditUIEvent {

	/** 弹窗界面状态 */
	val state: TimeTableEditUIState

	/** 切换当前生效的时间表 */
	fun switchToTimeTable(id: String)

	/** 打开名称输入框：新建 */
	fun beginCreateTimeTable()

	/** 打开名称输入框：复制 */
	fun beginCopyTimeTable(id: String)

	/** 打开名称输入框：改名 */
	fun beginRenameTimeTable(id: String)

	/** 进入节次编辑；内置配置会先复制一份 */
	fun beginEditTimeTable(id: String)

	/** 请求删除（弹确认） */
	fun requestDeleteTimeTable(id: String)

	fun confirmTimeTableName()
	fun dismissTimeTableNameDialog()
	fun confirmDeleteTimeTable()
	fun dismissDeleteTimeTable()

	/** 请求按当前时间表对齐课程（弹确认） */
	fun requestAlignLessons()
	fun confirmAlignLessons()
	fun dismissAlignLessons()

	fun saveTimeTableEditing()
	fun cancelTimeTableEditing()

	fun addTimeTablePeriod()
	fun removeTimeTablePeriod(index: Int)
}

/**
 * 时间表设置：一级为配置列表，二级为节次编辑
 */
@Composable
fun TimeTableEditUI(
	editor: TimeTableEditUIEvent,
	onDismiss: () -> Unit,
) {
	val catalog by TimeTableRepository.catalog.collectAsStateWithLifecycle()
	val state = editor.state

	Column(modifier = Modifier.fillMaxSize()) {
		when(state.mode) {
			TimeTableEditMode.LIST -> ConfigList(catalog = catalog, editor = editor, onDismiss = onDismiss)
			TimeTableEditMode.EDIT -> PeriodEditor(state = state, editor = editor)
		}
	}

	InputDialog(
		visible = state.nameDialogVisible,
		title = state.nameDialogTitle,
		label = "时间表名称",
		content = state.nameInput,
		onValueChange = { state.nameInput = it },
		onDismiss = editor::dismissTimeTableNameDialog,
		onConfirm = editor::confirmTimeTableName,
	)

	ConfirmDialog(
		visible = state.deleteTargetId != null,
		title = "删除时间表",
		message = "删除「${catalog.find(state.deleteTargetId)?.name ?: ""}」后不可恢复，是否继续？",
		onDismiss = editor::dismissDeleteTimeTable,
		onConfirm = editor::confirmDeleteTimeTable,
	)

	ConfirmDialog(
		visible = state.alignConfirmVisible,
		title = "课程对齐",
		message = "新时间表与当前课程时间不一致，是否将课程时间对齐到「${catalog.active().name}」？",
		onDismiss = editor::dismissAlignLessons,
		onConfirm = editor::confirmAlignLessons,
	)
}

@Composable
private fun ConfigList(
	catalog: TimeTableCatalog,
	editor: TimeTableEditUIEvent,
	onDismiss: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
			Text(text = "时间表", modifier = Modifier.align(Alignment.Center), style = Theme.textStyles.bodyLarge)
			TextButton(text = "关闭", colors = TextButtonColors.Danger, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp), onClick = onDismiss)
		}

		ColumnVerticalScroll(modifier = Modifier.weight(1f)) {
			catalog.configs.forEach { config ->
				ConfigItem(
					config = config,
					selected = config.id == catalog.activeId,
					editor = editor,
				)
			}
		}

		Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			PrimaryButton(text = "新建时间表", modifier = Modifier.fillMaxWidth(), onClick = { editor.beginCreateTimeTable() })
		}
	}
}

@Composable
private fun ConfigItem(
	config: TimeTableConfig,
	selected: Boolean,
	editor: TimeTableEditUIEvent,
) {
	Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
		.clickable { editor.switchToTimeTable(config.id) }
		.padding(horizontal = 8.dp, vertical = 4.dp)
	){
		if(selected) {
			Box(Modifier.fillMaxHeight().width(24.dp).padding(top = 4.dp, bottom = 4.dp, start = 4.dp, end = 12.dp).background(color = Theme.color.primary, shape = RoundedCornerShape(8.dp)))
		}else {
			Spacer(modifier = Modifier.width(24.dp))
		}

		Column(modifier = Modifier.fillMaxWidth()) {
			Text(text = config.name, style = Theme.textStyles.bodyStrong)
			Text(
				text = buildString {
					if(config.builtIn) append("内置 · ")
					append("${config.count} 节 · ")
					append("${config.timeTable.startTimeStr.first()} - ${config.timeTable.endTimeStr.last()}")
				},
				style = Theme.textStyles.caption,
				color = Theme.color.textSecondary,
			)

			Row(
				modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically,
			) {
				if(config.builtIn) {
					TextButton(text = "复制并编辑", colors = TextButtonColors.Normal, onClick = { editor.beginEditTimeTable(config.id) })
				}else {
					TextButton(text = "编辑", colors = TextButtonColors.Normal, type = ButtonType.Small, onClick = { editor.beginEditTimeTable(config.id) })
					TextButton(text = "改名", colors = TextButtonColors.Normal, type = ButtonType.Small, onClick = { editor.beginRenameTimeTable(config.id) })
					TextButton(text = "复制", colors = TextButtonColors.Normal, type = ButtonType.Small, onClick = { editor.beginCopyTimeTable(config.id) })
					TextButton(text = "删除", colors = TextButtonColors.Danger, type = ButtonType.Small, onClick = { editor.requestDeleteTimeTable(config.id) })
				}
			}
		}
	}
}

@Composable
private fun PeriodEditor(
	state: TimeTableEditUIState,
	editor: TimeTableEditUIEvent,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
			TextButton(text = "返回", onClick = editor::cancelTimeTableEditing)
			Text(
				text = state.editingName,
				modifier = Modifier.weight(1f),
				style = Theme.textStyles.bodyLarge,
				textAlign = TextAlign.Center,
			)
			TextButton(text = "保存", colors = TextButtonColors.Primary, onClick = editor::saveTimeTableEditing)
		}

		ColumnVerticalScroll(modifier = Modifier.weight(1f)) {
			state.periods.forEachIndexed { index, period ->
				Row(
					modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "第 ${index + 1} 节",
						modifier = Modifier.width(64.dp),
						style = Theme.textStyles.body,
					)

					Row(modifier = Modifier.weight(1F), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically)  {
						TimePick(
							hour = period.startHour,
							minute = period.startMinute,
							items = state.timeItems,
						)

						Text(text = " - ")

						TimePick(
							hour = period.endHour,
							minute = period.endMinute,
							items = state.timeItems,
						)
					}

					TextButton(text = "删除", colors = TextButtonColors.Danger, type = ButtonType.Small, onClick = { editor.removeTimeTablePeriod(index) })
				}
			}

			Spacer(modifier = Modifier.height(4.dp))

			TextButton(
				modifier = Modifier.padding(start = 8.dp),
				text = "添加一节",
				colors = TextButtonColors.Primary,
				onClick = { editor.addTimeTablePeriod() },
			)

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
