package com.qust.helper.viewmodel.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIEvent
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIState
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableInfo
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.utils.LessonUtils
import com.qust.helper.viewmodel.BaseViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import com.qust.helper.viewmodel.extend.toastWarning
import kotlinx.coroutines.flow.StateFlow

/**
 * 长按课表弹出的菜单类型
 */
enum class LessonPopupType { NONE, LESSON, BLANK }

open class LessonTableViewModel : BaseViewModel(), LessonEditUIEvent {

	val lessonTableInfo: StateFlow<LessonTableInfo> = LessonTableRepository.currentLessonTable

	val tableUIState = LessonTableUIState()

	val editUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	/** 长按菜单是否显示 */
	var isShowPopup by mutableStateOf(false)

	/** 长按菜单类型 */
	var popupType by mutableStateOf(LessonPopupType.NONE)

	/** 长按菜单弹出位置 */
	var popupPosition by mutableStateOf(IntOffset.Zero)

	/** 剪贴板中的课程 */
	var copyLessonValue by mutableStateOf<Lesson?>(null)

	/** 是否存在可粘贴的课程 */
	val hasCopyLesson: Boolean get() = copyLessonValue != null

	private var selectLessonIndex: Int = -1

	/** 选中的空白格子，编码为 timeSlot * 7 + week */
	private var selectCellIndex: Int = -1

	init {
		runBackGround {
			LessonTableRepository.refreshCurrentLessonTable()
		}
	}

	/**
	 * 长按课程
	 */
	fun longPressLesson(index: Int, lesson: Lesson, position: Offset){
		selectLessonIndex = index
		selectCellIndex = -1
		popupType = LessonPopupType.LESSON
		popupPosition = IntOffset(position.x.toInt(), position.y.toInt())
		isShowPopup = true
	}

	/**
	 * 长按空白格子
	 */
	fun longPressBlank(week: Int, timeSlot: Int, position: Offset){
		selectLessonIndex = -1
		selectCellIndex = timeSlot * 7 + week
		popupType = LessonPopupType.BLANK
		popupPosition = IntOffset(position.x.toInt(), position.y.toInt())
		isShowPopup = true
	}

	/**
	 * 关闭长按菜单
	 */
	fun dismissPopup(){
		isShowPopup = false
	}

	/**
	 * 编辑选中的课程
	 */
	fun editLesson(index: Int = selectLessonIndex){
		isShowPopup = false
		lessonTableInfo.value.lessons.getOrNull(index)?.let {
			clickLesson(index, it)
		}
	}

	/**
	 * 复制课程到剪贴板
	 */
	fun copyLesson(index: Int = selectLessonIndex){
		lessonTableInfo.value.lessons.getOrNull(index)?.let {
			copyLessonValue = it.copy()
			toastOK("已复制")
		}
		isShowPopup = false
	}

	/**
	 * 将剪贴板中的课程粘贴到指定格子
	 * @param index 格子编码 timeSlot * 7 + week
	 */
	fun pasteLesson(index: Int = selectCellIndex){
		val source = copyLessonValue
		if(source != null){
			val week = index % 7
			val timeSlot = index / 7
			val timeTable = lessonTableInfo.value.timeTable
			if(timeSlot in 0 until timeTable.count){
				val newLesson = source.copy(
					id = 0L,
					type = 1,
					reference = source.id,
					week = week,
					startMinute = timeTable.startMinute[timeSlot],
					endMinute = timeTable.endMinute[timeSlot],
				)
				runBackGround {
					if(LessonTableRepository.appendLessonToLessonTable(newLesson)){
						toastOK("粘贴完成")
					}else{
						toastError("粘贴课程失败")
					}
				}
			}
		}
		isShowPopup = false
	}

	/**
	 * 删除指定课程
	 */
	fun deleteLessonAt(index: Int = selectLessonIndex){
		lessonTableInfo.value.lessons.getOrNull(index)?.let { lesson ->
			runBackGround {
				if(LessonTableRepository.deleteLesson(lesson)){
					toastOK("删除完成")
				}else{
					toastError("删除课程失败")
				}
			}
		}
		isShowPopup = false
	}

	/**
	 * 在指定格子添加新课
	 * @param index 格子编码 timeSlot * 7 + week
	 */
	fun addLesson(index: Int = selectCellIndex){
		isShowPopup = false
		val week = index % 7
		val timeSlot = index / 7
		clickLesson(-1, null, week, timeSlot)
	}

	/**
	 * 点击课程/空白，打开编辑对话框
	 * @param week 新增课程时预填的周几
	 * @param timeSlot 新增课程时预填的时间节数
	 */
	fun clickLesson(index: Int, lesson: Lesson?, week: Int = 0, timeSlot: Int = -1){
		val selectLesson: Lesson
		if(lesson != null){
			selectLesson = lesson
			selectLessonIndex = index
		}else{
			selectLesson = Lesson(week = week)
			selectLessonIndex = -1
		}

		editUIState.lessonName = selectLesson.name
		editUIState.lessonPlace = selectLesson.place
		editUIState.lessonTeacher = selectLesson.teacher

		editUIState.week.value = selectLesson.week

		val timeTable = lessonTableInfo.value.timeTable
		val st = if(lesson != null) selectLesson.startMinute
			else if(timeSlot in 0 until timeTable.count) timeTable.startMinute[timeSlot]
			else 0
		val ed = if(lesson != null) selectLesson.endMinute
			else if(timeSlot in 0 until timeTable.count) timeTable.endMinute[timeSlot]
			else 0

		editUIState.startHour.value = (st / 60).toString()
		editUIState.startMinute.value = (st % 60).toString()
		editUIState.endHour.value = (ed / 60).toString()
		editUIState.endMinute.value = (ed % 60).toString()

		editUIState.colorIndex = selectLesson.colorLabel

		for(i in 0..< lessonTableInfo.value.totalWeek){
			editUIState.weeks[i] = (1L shl i) and selectLesson.weeks > 0
		}
		isEditLesson = true
	}

	override fun saveLesson() {
		val uiState = editUIState
		if(uiState.lessonName.isBlank()){
			toastWarning("请输入课程名称")
			return
		}

		val weeks = LessonUtils.getWeeksFromBooleans(uiState.weeks)
		if(weeks == 0L) {
			toastWarning("至少选择一周上课")
			return
		}
		val stH = uiState.startHour.value.toIntOrNull()
		val stM = uiState.startMinute.value.toIntOrNull()
		val edH = uiState.endHour.value.toIntOrNull()
		val edM = uiState.endMinute.value.toIntOrNull()
		if(stH == null || stM == null || edH == null || edM == null){
			toastWarning("请输入正确的上课时间")
			return
		}

		val stMinutes = stH * 60 + stM
		val edMinutes = edH * 60 + edM
		if(edMinutes <= stMinutes){
			toastWarning("开始时间必须小于结束时间")
			return
		}

		runBackGround {
			if(selectLessonIndex == -1){
				val newLesson = Lesson(
					type = 1,
					colorLabel = uiState.colorIndex,
					weeks = weeks,
					week = uiState.week.value,
					startMinute = stMinutes,
					endMinute = edMinutes,
					name = uiState.lessonName,
					place = uiState.lessonPlace,
					teacher = uiState.lessonTeacher,
					remark = "",
				)
				val success = LessonTableRepository.appendLessonToLessonTable(newLesson)
				if(success){
					toastOK("保存完成")
				}else{
					toastError("保存课程失败")
				}
			}else{
				val newLesson = lessonTableInfo.value.lessons[selectLessonIndex].copy(
					colorLabel = uiState.colorIndex,
					weeks = weeks,
					week = uiState.week.value,
					startMinute = stMinutes,
					endMinute = edMinutes,
					name = uiState.lessonName,
					place = uiState.lessonPlace,
					teacher = uiState.lessonTeacher,
				)

				if(LessonTableRepository.updateLessonTableLesson(newLesson)){
					toastOK("保存完成")
					// 更新课程不会自动触发UI更新，需要手动刷新
					tableUIState.refreshLessonTable()
				}else{
					toastError("保存课程失败")
				}
			}
			isEditLesson = false
		}
	}

	override fun cancel() {
		isEditLesson = false
	}
}