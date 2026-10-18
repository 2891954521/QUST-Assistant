package com.qust.helper.next.ui.business.lesson.timeTableEdit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.entity.lesson.TimeTableIssue
import com.qust.helper.next.entity.lesson.TimeTableValidator

/** 弹窗层级：配置列表 / 节次编辑 */
enum class TimeTableEditMode { LIST, EDIT }

/** 名称输入框要执行的动作 */
enum class TimeTableNameAction { CREATE, COPY, RENAME }

/**
 * 一节的可编辑时间
 */
class TimeTablePeriodState {

	val startHour = mutableStateOf("08")
	val startMinute = mutableStateOf("00")
	val endHour = mutableStateOf("08")
	val endMinute = mutableStateOf("50")

	fun load(startMinuteOfDay: Int, endMinuteOfDay: Int) {
		startHour.value = hourText(startMinuteOfDay)
		startMinute.value = minuteText(startMinuteOfDay)
		endHour.value = hourText(endMinuteOfDay)
		endMinute.value = minuteText(endMinuteOfDay)
	}

	/** @return HHMM；输入非法返回 null */
	fun startValue(): Int? = parse(startHour.value, startMinute.value)

	/** @return HHMM；输入非法返回 null */
	fun endValue(): Int? = parse(endHour.value, endMinute.value)

	private fun parse(hour: String, minute: String): Int? {
		val h = hour.toIntOrNull() ?: return null
		val m = minute.toIntOrNull() ?: return null
		if(h !in 0..23 || m !in 0..59) return null
		return h * 100 + m
	}

	private fun hourText(minuteOfDay: Int): String = (minuteOfDay / 60).toString().padStart(2, '0')

	private fun minuteText(minuteOfDay: Int): String = (minuteOfDay % 60).toString().padStart(2, '0')
}

/**
 * 时间表设置弹窗的界面状态
 */
class TimeTableEditUIState {

	var mode by mutableStateOf(TimeTableEditMode.LIST)

	/** 正在编辑的配置 id 与名称（二级界面） */
	var editingId by mutableStateOf("")
	var editingName by mutableStateOf("")

	val periods = mutableStateListOf<TimeTablePeriodState>()

	/** 名称输入框 */
	var nameDialogVisible by mutableStateOf(false)
	var nameDialogTitle by mutableStateOf("")
	var nameInput by mutableStateOf("")
	var nameAction by mutableStateOf<TimeTableNameAction?>(null)
	var nameActionTargetId by mutableStateOf<String?>(null)

	/** 删除确认的目标 id */
	var deleteTargetId by mutableStateOf<String?>(null)

	/** 对齐课程确认 */
	var alignConfirmVisible by mutableStateOf(false)

	/** 时间下拉候选：00:00 ~ 23:55，5 分钟步长 */
	val timeItems: List<String> = List(24 * 12) { i ->
		val minuteOfDay = i * 5
		"${(minuteOfDay / 60).toString().padStart(2, '0')}:${(minuteOfDay % 60).toString().padStart(2, '0')}"
	}

	fun loadPeriods(timeTable: TimeTable) {
		periods.clear()
		timeTable.startMinute.forEachIndexed { i, start ->
			periods += TimeTablePeriodState().also { it.load(start, timeTable.endMinute[i]) }
		}
	}

	/** @return null 表示校验通过 */
	fun issue(): TimeTableIssue? {
		val start = ArrayList<Int>(periods.size)
		val end = ArrayList<Int>(periods.size)
		periods.forEachIndexed { i, row ->
			val s = row.startValue() ?: return TimeTableIssue(i, "第 ${i + 1} 节开始时间不正确")
			val e = row.endValue() ?: return TimeTableIssue(i, "第 ${i + 1} 节结束时间不正确")
			start += s
			end += e
		}
		return TimeTableValidator.validate(start, end)
	}

	/** 校验通过时返回时间表，否则返回 null */
	fun toTimeTable(): TimeTable? {
		if(issue() != null) return null
		return TimeTable(
			periods.map { it.startValue()!! }.toTypedArray(),
			periods.map { it.endValue()!! }.toTypedArray(),
		)
	}
}
