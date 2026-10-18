package com.qust.helper.next.ui.page.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.LessonTableData
import com.qust.helper.next.module.lesson.LessonUtils
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.dialog.BottomDialog
import com.qust.helper.next.ui.business.lesson.LessonTable
import com.qust.helper.next.ui.business.lesson.lessonEdit.LessonEditUI
import com.qust.helper.next.ui.business.lesson.lessonEdit.LessonEditUIEvent
import com.qust.helper.next.ui.business.lesson.lessonEdit.LessonEditUIState
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.BackHandler
import com.qust.helper.next.ui.drawables.Drawables
import com.qust.helper.next.ui.drawables.GridView
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState


@Preview
@Composable
private fun LessonTablePreview() = AppPreview(::LessonTableUI)

class LessonTablePage : AppPage<LessonTableViewModel>(title = "学期课表", icon = Drawables.GridView, viewModelClass = LessonTableViewModel::class) {
	@Composable
	override fun Content(viewModel: LessonTableViewModel) = LessonTableUI(viewModel)
}

@Composable
private fun LessonTableUI(viewModel: LessonTableViewModel) {
	val lessonTableInfo by viewModel.lessonTableInfo.collectAsState()

	BackHandler(viewModel.isEditLesson) {
		viewModel.isEditLesson = false
	}

	Box(Modifier.fillMaxSize()) {

		LessonTable(
			data = lessonTableInfo,
			settings = remember { LessonTableRepository.LessonTableSetting() },
			onCellClick = {
				viewModel.clickLesson(it.lesson)
			}
		){ i, it ->

		}

		Button(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), onClick = {
			viewModel.clickLesson(null)
		}){
			Text("添加课程")
		}
	}

	LessonEditDialog(viewModel)
}


@Composable
private fun LessonEditDialog(viewModel: LessonTableViewModel) {
	BottomDialog(isExpanded = viewModel.isEditLesson) {
		LessonEditUI(
			uiState = viewModel.editUIState,
			uiEvent = viewModel,
			onDismiss = { viewModel.cancelEditLesson() },
		)
	}
}

class LessonTableViewModel : BaseViewModel(), LessonEditUIEvent {

	val lessonTableInfo: StateFlow<LessonTableData> = LessonTableRepository.currentLessonTable

	val editUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	private var selectLesson: Lesson? = null

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		runInBackground {
			LessonTableRepository.refreshCurrentLessonTable()
		}
	}

	fun clickLesson(lesson: Lesson?){
		if(lesson != null){
			editUIState.lessonName = lesson.name
			editUIState.lessonPlace = lesson.place
			editUIState.lessonTeacher = lesson.teacher

			editUIState.week.value = lesson.week

			editUIState.startHour.value = (lesson.startMinute / 60).toString()
			editUIState.startMinute.value = (lesson.startMinute % 60).toString()
			editUIState.endHour.value = (lesson.endMinute / 60).toString()
			editUIState.endMinute.value = (lesson.endMinute % 60).toString()

			editUIState.colorIndex = lesson.colorLabel

			for(i in 0..< lessonTableInfo.value.totalWeek){
				editUIState.weeks[i] = (1L shl i) and lesson.weeks > 0
			}
		}else{
			editUIState.lessonName = ""
			editUIState.lessonPlace = ""
			editUIState.lessonTeacher = ""

			editUIState.week.value = 0

			editUIState.startHour.value = "8"
			editUIState.startMinute.value = "00"
			editUIState.endHour.value = "9"
			editUIState.endMinute.value = "00"

			editUIState.colorIndex = 0

			for(i in 0..< lessonTableInfo.value.totalWeek){
				editUIState.weeks[i] = false
			}
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

		runInBackground {
			val selectLesson = selectLesson
			if(selectLesson == null){
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
					toastSuccess("保存完成")
				}else{
					toastError("保存课程失败")
				}
			}else{
				val newLesson = selectLesson.copy(
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
					toastSuccess("保存完成")
				}else{
					toastError("保存课程失败")
				}
			}
			isEditLesson = false
		}
	}

	fun cancelEditLesson() {
		isEditLesson = false
	}
}