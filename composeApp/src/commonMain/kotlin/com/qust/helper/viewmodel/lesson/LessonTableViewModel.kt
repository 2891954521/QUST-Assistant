package com.qust.helper.viewmodel.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.model.lessonTable.LessonTableModel
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIEvent
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonEditUIState
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.utils.LessonUtils
import com.qust.helper.viewmodel.BaseViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import com.qust.helper.viewmodel.extend.toastWarning

open class LessonTableViewModel : BaseViewModel() {

	val lessonTableUIState = LessonTableUIState()

	val lessonEditUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	val lessonEditUIEvent = object : LessonEditUIEvent {
		override fun saveLesson() {
			val uiState = lessonEditUIState
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
				val success = LessonTableModel.saveLesson(Lesson(
					type = 1,
					reference = 0L,
					lessonId = "",
					colorLabel = uiState.colorIndex,
					weeks = weeks,
					week = uiState.week.value,
					startMinute = stMinutes,
					endMinute = edMinutes,
					name = uiState.lessonName,
					place = uiState.lessonPlace,
					teacher = uiState.lessonTeacher,
					remark = "",
				))
				if(success){
					toastOK("保存完成")
				}else{
					toastError("保存课程失败")
				}
			}
		}

		override fun cancel() {
			isEditLesson = false
		}

	}

	init {
		runBackGround {
			lessonTableUIState.setLessonTable(LessonTableModel.getAllLesson())
		}
	}
}