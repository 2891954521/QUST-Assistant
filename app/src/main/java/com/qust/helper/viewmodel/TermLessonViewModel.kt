package com.qust.helper.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.qust.helper.R
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.ui.common.ToastAble
import com.qust.helper.ui.widget.LessonRender

class TermLessonViewModel(
	activity: Activity,
	private val lessonTableViewModel: LessonTableViewModel
): ViewModel() {

	val uiState = TermLessonUIState(
		totalWeek = lessonTableViewModel.lessonTable.value.totalWeek,
		lessonRender = LessonRender().also { it.lessonTable = lessonTableViewModel.lessonTable.value }
	)

	val uiEvent = object: TermLessonUIEvent{
		override fun showEdit() { uiState.isShowEdit = true }
		override fun hideEdit() { uiState.isShowEdit = false }
		override fun clickLesson(select: LessonRender.SelectLesson) { this@TermLessonViewModel.clickLesson(select) }
		override fun longClickLesson(select: LessonRender.SelectLesson, week: Int){ this@TermLessonViewModel.longClickLesson(select, week) }
		override fun addLesson() { this@TermLessonViewModel.addLesson() }
		override fun editLesson() { this@TermLessonViewModel.editLesson() }
		override fun copyLesson() { this@TermLessonViewModel.copyLesson() }
		override fun pasteLesson() { this@TermLessonViewModel.pasteLesson() }
		override fun deleteLesson() { this@TermLessonViewModel.deleteLesson() }
		override fun saveLesson() { this@TermLessonViewModel.saveLesson() }
		override fun toastOK(message: String){ toastAble.toast(R.drawable.tips_finish, message) }
		override fun toastWarning(message: String) { toastAble.toast(R.drawable.tips_warning, message) }
		override fun toastError(message: String) { toastAble.toast(R.drawable.tips_error, message) }
	}

	private val toastAble = ToastAble(activity)

	private var currentDayOfWeek = 0
	private var currentTimeSlot = 0

	private var copyLesson: Lesson = Lesson.EMPTY_LESSON
	private var selectLesson: Lesson = Lesson.EMPTY_LESSON

	fun clickLesson(select: LessonRender.SelectLesson? = null) {
		if(select != null){
			currentDayOfWeek = select.dayOfWeek
			currentTimeSlot = select.timeSlot
			if(select.lesson != null) {
				selectLesson = select.lesson
				uiState.isNewLesson = false
			}else{
				val lesson = Lesson(type = 1)
				lessonTableViewModel.lessonTable.value.getLessonGroupNotNull(currentDayOfWeek, currentTimeSlot).addLesson(lesson)
				selectLesson = lesson
				uiState.isNewLesson = true
			}
		}

		uiState.name = selectLesson.name
		uiState.place = selectLesson.place
		uiState.teacher = selectLesson.teacher

		uiState.len = selectLesson.len
		uiState.color = selectLesson.color

		for(i in 0..< uiState.totalWeek){
			uiState.week[i] = (1L shl i) and selectLesson.week > 0
		}

		uiState.isShowEdit = true
	}

	fun longClickLesson(select: LessonRender.SelectLesson, week: Int){
		currentDayOfWeek = select.dayOfWeek
		currentTimeSlot = select.timeSlot
		if(select.lesson != null) {
			selectLesson = select.lesson
			uiState.isNewLesson = false
		}else{
			val lesson = Lesson(type = 1)
			lessonTableViewModel.lessonTable.value.getLessonGroupNotNull(currentDayOfWeek, currentTimeSlot).addLesson(lesson)
			selectLesson = lesson
			uiState.isNewLesson = true
		}

		uiState.pasteEnable = copyLesson !== Lesson.EMPTY_LESSON

		if(selectLesson !== Lesson.EMPTY_LESSON) {
			uiState.copyEnable = true
			uiState.deleteEnable = true
			uiState.addLessonEnable = selectLesson.week and (1L shl week) != 1L
		} else {
			uiState.copyEnable = false
			uiState.deleteEnable = false
			uiState.addLessonEnable = false
		}
		uiState.isShowPopup = true
	}

	fun addLesson(){
		clickLesson()
		uiState.isShowPopup = false
	}

	fun editLesson(){
		clickLesson()
		uiState.isShowPopup = false
	}

	fun copyLesson(){
		copyLesson = selectLesson.copy()
		uiState.isShowPopup = false
	}

	fun pasteLesson(){
		if(copyLesson !== Lesson.EMPTY_LESSON){
			lessonTableViewModel.lessonTable.value.getLessonGroupNotNull(currentDayOfWeek, currentTimeSlot).addLesson(copyLesson.copy())
			lessonTableViewModel.saveLessonTable()
			uiState.lessonRender.lessonTable = lessonTableViewModel.lessonTable.value
		}
		uiState.isShowPopup = false
	}

	fun deleteLesson(){
		lessonTableViewModel.lessonTable.value.lessons[currentDayOfWeek][currentTimeSlot]?.let {
			it.removeLesson(selectLesson)
			lessonTableViewModel.saveLessonTable()
			uiState.lessonRender.lessonTable = lessonTableViewModel.lessonTable.value
		}
		uiState.isShowPopup = false
	}

	fun saveLesson() {
		var week = 0L
		var tmp = 1L
		for(i in uiState.week){
			if(i) week = week or tmp
			tmp = tmp shl 1
		}

		if(week == 0L) {
			uiEvent.toastWarning("请选择上课时间！")
			return
		}

		var hasEdit: Boolean = uiState.isNewLesson

		if(week != selectLesson.week || uiState.len != selectLesson.len) {
			selectLesson.len = uiState.len
			selectLesson.week = week
			hasEdit = true
		}

		if(uiState.name != selectLesson.name) {
			selectLesson.name = uiState.name
			hasEdit = true
		}

		if(uiState.place != selectLesson.place) {
			selectLesson.place = uiState.place
			hasEdit = true
		}

		if(uiState.teacher != selectLesson.teacher) {
			selectLesson.teacher = uiState.teacher
			hasEdit = true
		}

		var hasEditColor = false
		if(uiState.color != selectLesson.color) {
			selectLesson.color = uiState.color
			hasEditColor = true
		}

		if(hasEdit) selectLesson.type = 1

		if(hasEdit || hasEditColor) {
			uiState.lessonRender.lessonTable = lessonTableViewModel.lessonTable.value
			lessonTableViewModel.saveLessonTable()
		}

		uiState.isShowEdit = false
	}
}

class TermLessonUIState(
	totalWeek: Int = 1,
	val lessonRender: LessonRender = LessonRender()
) {
	var totalWeek by mutableIntStateOf(totalWeek)

	var name by mutableStateOf("")
	var place by mutableStateOf("")
	var teacher by mutableStateOf("")

	var len by mutableIntStateOf(1)
	var color by mutableIntStateOf(0)

	var week = List(totalWeek){ false }.toMutableStateList()

	var isShowEdit by mutableStateOf(false)
	var isNewLesson by mutableStateOf(false)
	var isShowPopup by mutableStateOf(false)

	var copyEnable = false
	var pasteEnable = false
	var deleteEnable = false
	var addLessonEnable = false
}

interface TermLessonUIEvent{
	fun showEdit(){ }
	fun hideEdit(){ }

	fun clickLesson(select: LessonRender.SelectLesson){ }
	fun longClickLesson(select: LessonRender.SelectLesson, week: Int){ }

	fun addLesson(){ }
	fun editLesson(){ }
	fun copyLesson(){ }
	fun pasteLesson(){ }
	fun deleteLesson(){ }

	fun saveLesson(){ }

	fun toastOK(message: String){ }
	fun toastWarning(message: String){ }
	fun toastError(message: String){ }
}