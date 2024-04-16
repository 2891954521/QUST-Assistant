package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Data
import com.qust.helper.data.room.LessonDatabase
import com.qust.helper.data.room.LessonInfo
import com.qust.helper.data.room.LessonInfoGroup
import com.qust.helper.data.room.Mark
import com.qust.helper.ui.widget.ToastContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class GetAcademicViewModel(application: Application): BaseEasViewModel(application) {

	private val lessonData = LessonDatabase.getInstance(application)

	private val showModeGroup: Array<List<AcademicGroup>> = arrayOf(emptyList(), emptyList())

	val uiState = AcademicUIState(_dialogText, toastContent)

	val uiEvent = object : AcademicUIEvent {
		override fun queryData() { this@GetAcademicViewModel.queryData() }

		override fun changeGroup() { this@GetAcademicViewModel.changeGroupMode() }
		override fun clickGroup(index: Int) { this@GetAcademicViewModel.clickGroup(index) }
		override fun clickLesson(group: Int, lesson: Int) { this@GetAcademicViewModel.clickLesson(group, lesson) }

		override fun sortBy(index: Int, type: Int) { this@GetAcademicViewModel.sortBy(index, type) }
		override fun sortType(index: Int) { this@GetAcademicViewModel.changeSortType(index) }
	}

	fun loadData(){
		if(showModeGroup[0].isEmpty()){
			viewModelScope.launch { withContext(Dispatchers.IO) {
				try {
					showModeGroup[0] = lessonData.lessonInfoDao().selectGroups().map { AcademicGroup(it) }
					uiState.lessonGroups = showModeGroup[0]
				}catch(e: Exception){ e.printStackTrace() }
			} }
		}
	}

	/**
	 * 切换展示模式
	 */
	fun changeGroupMode() {
		uiState.showMode = if(uiState.showMode == 0) 1 else 0
		if(uiState.showMode == 1 && showModeGroup[1].isEmpty() && showModeGroup[0].isNotEmpty()) {
			viewModelScope.launch { withContext(Dispatchers.IO) {
				sortByTerm()
				uiState.lessonGroups = showModeGroup[1]
			} }
		}else{
			uiState.lessonGroups = showModeGroup[uiState.showMode]
		}
	}

	fun clickGroup(index: Int){
		val item = uiState.lessonGroups[index]
		if(!item.hasQuery){
			viewModelScope.launch {
				withContext(Dispatchers.IO) {
					item.lessons = if(uiState.showMode == 0) lessonData.lessonInfoDao().selectByGroup(item.groupInfo.group) else lessonData.lessonInfoDao().selectByIndex(item.groupInfo.group)
					sort(item)
					item.hasQuery = true
					item.isExpand = !item.isExpand
				}
			}
		}else{
			item.isExpand = !item.isExpand
		}
	}

	fun clickLesson(group: Int, lesson: Int) {
		val item = uiState.lessonGroups[group]
		if(item.lessonMarks[lesson] === Mark.EMPTY_MARK){
			viewModelScope.launch { withContext(Dispatchers.IO) {
				val marks = lessonData.markDao().selectByKchId(item.lessons[lesson].kchId)
				if(marks.isNotEmpty()){
					item.lessonMarks[lesson] = marks.maxBy { it.time }
				}
				item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
			} }
		}else{
			item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
		}
	}

	fun sortBy(index: Int, type: Int){
		val group = showModeGroup[uiState.showMode][index]
		group.sortBy = type
		sort(group)
	}

	fun changeSortType(index: Int){
		val group = showModeGroup[uiState.showMode][index]
		group.sortType = if(group.sortType == 1) -1 else 1
		sort(group)
	}

	fun queryData() {
		viewModelScope.launch {
			showDialog("查询中")
			withContext(Dispatchers.IO){
				if(checkLogin()){
					val pair = easAccount.getAcademic()
					try {
						lessonData.lessonInfoDao().clear()
						lessonData.lessonInfoDao().clearGroups()
						lessonData.lessonInfoDao().insertAllGroups(pair.first)
						lessonData.lessonInfoDao().insertAll(pair.second.toList())
					} catch(_: IOException) { }
					uiState.showMode = 0
					showModeGroup[0] = pair.first.map { AcademicGroup(it) }
					showModeGroup[1] = emptyList()
					uiState.lessonGroups = showModeGroup[0]
					toastOK("查询完成")
				}
			}
			clearDialog()
		}
	}

	private fun sort(group: AcademicGroup){
		group.lessons = when(group.sortBy){
			0 ->  group.lessons.sortedWith { a, b -> a.mark.compareTo(b.mark) * group.sortType }
			1 -> group.lessons.sortedWith { a, b ->
				val v = a.credit.compareTo(b.credit) * group.sortType
				if(v == 0) a.mark.compareTo(b.mark) * group.sortType
				else v
			}
			2 -> group.lessons.sortedWith { a, b ->
				val v = a.status.compareTo(b.status) * group.sortType
				if(v == 0) a.mark.compareTo(b.mark) * group.sortType
				else v
			}
			else -> group.lessons
		}
	}

	/**
	 * 按修读学期分组
	 */
	private suspend fun sortByTerm() {
		val builders: Array<LessonInfoGroup.Builder> = Array(Data.TermName.size){
			LessonInfoGroup.Builder(group = it, type = Data.TermName[it])
		}

		val entranceTime: Int = easAccount.entranceTime
		if(entranceTime == -1) {
			toastWarning("未设置入学年份")
		}

		for(index in lessonData.lessonInfoDao().selectIndex()){
			if(index.group >= 0 && index.group < builders.size){
				val group = builders[index.group]
				group.totalCounts = index.totalCounts
				group.passedCounts = index.passedCounts
				group.requireCredits = index.requireCredits
				group.obtainedCredits = index.obtainedCredits
			}
		}

		showModeGroup[1] = List(builders.size){ AcademicGroup(builders[it].build()) }
	}
}

class AcademicGroup(val groupInfo: LessonInfoGroup){
	var sortBy = 0
	var hasQuery = false

	var isExpand by mutableStateOf(false)
	var sortType by mutableIntStateOf(-1)

	val _lessons: MutableState<List<LessonInfo>> = mutableStateOf(emptyList())
	var lessons: List<LessonInfo>
		get() = _lessons.value
		set(value) {
			isLessonExpand = value.map { false }.toMutableStateList()
			lessonMarks = Array(value.size) { Mark.EMPTY_MARK }
			_lessons.value = value
		}

	var isLessonExpand = lessons.map { false }.toMutableStateList()
	var lessonMarks: Array<Mark> = emptyArray()
}

class AcademicUIState(
	var dialogText: MutableState<String>,
	var toastContent: MutableState<ToastContent>,
) {
	/**
	 * 展示模式: 0 - 按课程类型，1 - 按修读年份
	 */
	var showMode by mutableIntStateOf(0)
	var lessonGroups by mutableStateOf(listOf<AcademicGroup>())
}

interface AcademicUIEvent{
	fun queryData() { }
	fun changeGroup() { }

	fun clickGroup(index: Int) { }
	fun clickLesson(group: Int, lesson: Int) { }

	fun sortBy(index: Int, type: Int) { }
	fun sortType(index: Int) { }
}