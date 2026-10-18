package com.qust.helper.next.ui.business.lesson.timeTableEdit

import com.qust.helper.next.entity.lesson.TimeTableValidator
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.repository.TimeTableRepository
import com.qust.helper.next.ui.component.overlay.toast.ToastAble
import com.qust.helper.next.ui.viewmodel.AsyncAble

/**
 * 时间表设置弹窗的交互实现
 *
 * 从设置页的 ViewModel 中拆出，避免与设置页无关的逻辑堆在 SettingsViewModel 里；
 * 由设置页的 ViewModel 实例化（把 toast 与 async 能力传进来），再交给 TimeTableEditUI 使用。
 */
class TimeTableEditController(
	private val toast: ToastAble,
	private val async: AsyncAble,
) : TimeTableEditUIEvent {

	override val state = TimeTableEditUIState()

	override fun switchToTimeTable(id: String) {
		val before = TimeTableRepository.currentTimeTable.value
		TimeTableRepository.switchTo(id)
		val after = TimeTableRepository.currentTimeTable.value
		if(after == before) return

		// 时间表发生变化、且已保存课程仍对应旧时间表时，询问是否对齐
		if(LessonTableRepository.lessonTimeTable != after) {
			state.alignConfirmVisible = true
		}
	}

	override fun beginCreateTimeTable() {
		showNameDialog(TimeTableNameAction.CREATE, null, "新建时间表", "我的时间表")
	}

	override fun beginCopyTimeTable(id: String) {
		val name = TimeTableRepository.catalog.value.find(id)?.name ?: return
		showNameDialog(TimeTableNameAction.COPY, id, "复制时间表", "$name 副本")
	}

	override fun beginRenameTimeTable(id: String) {
		val name = TimeTableRepository.catalog.value.find(id)?.name ?: return
		showNameDialog(TimeTableNameAction.RENAME, id, "重命名时间表", name)
	}

	override fun beginEditTimeTable(id: String) {
		val config = TimeTableRepository.catalog.value.find(id) ?: return
		if(config.builtIn) {
			// 内置配置只读，先复制一份再编辑
			val newId = TimeTableRepository.copyConfig(id, "${config.name} 副本")
			if(newId == null) {
				toast.toastWarning("复制时间表失败")
				return
			}
			openEditor(newId)
		}else {
			openEditor(config.id)
		}
	}

	override fun requestDeleteTimeTable(id: String) {
		state.deleteTargetId = id
	}

	override fun confirmTimeTableName() {
		state.nameDialogVisible = false

		val action = state.nameAction ?: return
		val name = state.nameInput.trim()
		if(name.isEmpty()) {
			toast.toastWarning("请输入名称")
			return
		}
		if(name.length > MAX_NAME_LENGTH) {
			toast.toastWarning("名称不能超过 $MAX_NAME_LENGTH 个字符")
			return
		}

		when(action) {
			TimeTableNameAction.CREATE -> {
				val id = TimeTableRepository.createConfig(name, TimeTableRepository.currentTimeTable.value)
				if(id == null) {
					toast.toastWarning("新建失败，配置数量已达上限")
				}else {
					openEditor(id)
				}
			}

			TimeTableNameAction.COPY -> {
				val id = state.nameActionTargetId?.let { TimeTableRepository.copyConfig(it, name) }
				if(id == null) {
					toast.toastWarning("复制失败，配置数量已达上限")
				}else {
					openEditor(id)
				}
			}

			TimeTableNameAction.RENAME -> {
				val targetId = state.nameActionTargetId
				if(targetId == null || !TimeTableRepository.renameConfig(targetId, name)) {
					toast.toastWarning("改名失败")
				}else {
					toast.toastSuccess("已改名")
				}
			}
		}
	}

	override fun dismissTimeTableNameDialog() {
		state.nameDialogVisible = false
	}

	override fun confirmDeleteTimeTable() {
		val id = state.deleteTargetId ?: return
		state.deleteTargetId = null

		val wasActive = TimeTableRepository.catalog.value.activeId == id
		if(!TimeTableRepository.deleteConfig(id)) {
			toast.toastWarning("删除失败")
			return
		}
		toast.toastSuccess(if(wasActive) "已删除，并切换到${TimeTableRepository.active().name}" else "已删除")
	}

	override fun dismissDeleteTimeTable() {
		state.deleteTargetId = null
	}

	override fun requestAlignLessons() {
		state.alignConfirmVisible = true
	}

	override fun confirmAlignLessons() {
		state.alignConfirmVisible = false

		async.runInBackground {
			val count = async.loadingSuspend("对齐中") { LessonTableRepository.alignLessonsToTimeTable() } ?: 0
			if(count > 0) {
				toast.toastSuccess("已对齐 $count 门课程")
			}else {
				toast.toastWarning("没有需要对齐的课程")
			}
		}
	}

	override fun dismissAlignLessons() {
		state.alignConfirmVisible = false
	}

	override fun saveTimeTableEditing() {
		state.issue()?.let {
			toast.toastWarning(it.message)
			return
		}
		val timeTable = state.toTimeTable() ?: return

		if(TimeTableRepository.updateConfig(state.editingId, timeTable)) {
			toast.toastSuccess("已保存")
			state.mode = TimeTableEditMode.LIST
		}else {
			toast.toastWarning("保存失败")
		}
	}

	override fun cancelTimeTableEditing() {
		state.mode = TimeTableEditMode.LIST
	}

	override fun addTimeTablePeriod() {
		if(state.periods.size >= TimeTableValidator.MAX_PERIODS) {
			toast.toastWarning("最多 ${TimeTableValidator.MAX_PERIODS} 节")
			return
		}

		val lastEnd = state.periods.lastOrNull()?.endValue()?.let { TimeTableValidator.toMinute(it) } ?: -1
		val startOfDay = if(lastEnd < 0) 8 * 60 else lastEnd + 10
		val endOfDay = startOfDay + 45
		if(endOfDay >= 24 * 60) {
			toast.toastWarning("时间超出当天范围，无法添加")
			return
		}
		state.periods += TimeTablePeriodState().also { it.load(startOfDay, endOfDay) }
	}

	override fun removeTimeTablePeriod(index: Int) {
		if(state.periods.size <= TimeTableValidator.MIN_PERIODS) {
			toast.toastWarning("至少保留一节")
			return
		}
		if(index in state.periods.indices) {
			state.periods.removeAt(index)
		}
	}

	private fun openEditor(id: String) {
		val config = TimeTableRepository.catalog.value.find(id) ?: return
		state.editingId = config.id
		state.editingName = config.name
		state.loadPeriods(config.timeTable)
		state.mode = TimeTableEditMode.EDIT
	}

	private fun showNameDialog(action: TimeTableNameAction, targetId: String?, title: String, defaultName: String) {
		state.nameAction = action
		state.nameActionTargetId = targetId
		state.nameDialogTitle = title
		state.nameInput = defaultName
		state.nameDialogVisible = true
	}

	private companion object {
		const val MAX_NAME_LENGTH = 24
	}
}
