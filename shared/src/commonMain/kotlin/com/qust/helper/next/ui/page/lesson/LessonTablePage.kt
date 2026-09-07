package com.qust.helper.next.ui.page.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.LessonTableInfo
import com.qust.helper.next.module.lesson.LessonUtils
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.dialog.BottomDialog
import com.qust.helper.next.ui.business.lesson.LessonTableUI
import com.qust.helper.next.ui.business.lesson.LessonTableUIState
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


@Preview
@Composable
private fun LessonTablePreview() = AppPreview(::LessonTableUI)

class LessonTablePage : AppPage<LessonTableViewModel>(title = "学期课表", icon = Drawables.GridView, viewModelClass = LessonTableViewModel::class) {
	@Composable
	override fun Content(viewModel: LessonTableViewModel) = LessonTableUI(viewModel)
}

@Composable
private fun LessonTableUI(viewModel: LessonTableViewModel) {
	BackHandler(viewModel.isEditLesson) {
		viewModel.isEditLesson = false
	}

	Box(Modifier.fillMaxSize()) {

		LessonTableUI(viewModel.tableUIState){ i, it ->
			viewModel.clickLesson(i, it)
		}

		Button(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), onClick = {
			viewModel.clickLesson(-1, null)
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

	val lessonTableInfo: StateFlow<LessonTableInfo> = LessonTableRepository.currentLessonTable

	val tableUIState = LessonTableUIState()

	val editUIState = LessonEditUIState()

	var isEditLesson by mutableStateOf(false)

	private var selectLessonIndex: Int = -1

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		runInBackground {
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

		runInBackground {
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
					toastSuccess("保存完成")
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
					toastSuccess("保存完成")
					// 更新课程不会自动触发UI更新，需要手动刷新
					tableUIState.refreshLessonTable()
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