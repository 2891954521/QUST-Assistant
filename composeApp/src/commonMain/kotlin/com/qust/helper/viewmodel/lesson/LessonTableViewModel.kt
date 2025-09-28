package com.qust.helper.viewmodel.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

open class LessonTableViewModel : BaseViewModel(), LessonEditUIEvent {

	val lessonTableInfo: StateFlow<LessonTableInfo> = LessonTableRepository.currentLessonTable

	val tableUIState = LessonTableUIState()

	val editUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	private var selectLessonIndex: Int = -1

	init {
		runBackGround {
			LessonTableRepository.refreshCurrentLessonTable()
		}
	}

	fun clickLesson(index: Int, lesson: Lesson?){
		val selectLesson: Lesson
		if(lesson != null){
			selectLesson = lesson
			selectLessonIndex = index
		}else{
			selectLesson = Lesson()
			selectLessonIndex = -1
		}

		editUIState.lessonName = selectLesson.name
		editUIState.lessonPlace = selectLesson.place
		editUIState.lessonTeacher = selectLesson.teacher

		editUIState.week.value = selectLesson.week

		editUIState.startHour.value = (selectLesson.startMinute / 60).toString()
		editUIState.startMinute.value = (selectLesson.startMinute % 60).toString()
		editUIState.endHour.value = (selectLesson.endMinute / 60).toString()
		editUIState.endMinute.value = (selectLesson.endMinute % 60).toString()

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