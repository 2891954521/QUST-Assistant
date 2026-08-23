package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.eas.AcademicGroup
import com.qust.helper.entity.eas.AcademicInfo
import com.qust.helper.entity.eas.Mark
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.AcademicModel
import com.qust.helper.model.eas.MarkModel
import com.qust.helper.utils.Logger
import com.qust.helper.viewmodel.extend.toastWarning

class QueryAcademicViewModel: BaseEasViewModel(), AcademicUIEvent {

	val uiState = AcademicUIState()

	private val showModeGroup: Array<List<AcademicGroupUIState>> = arrayOf(emptyList(), emptyList())

	init {
		runBackGround {
			try {
				showModeGroup[0] = AcademicModel.getGroups().map { AcademicGroupUIState(it) }
				uiState.lessonGroups = showModeGroup[0]
			}catch(e: Exception){ Logger.e(e = e) }
		}
	}

	/**
	 * 切换展示模式
	 */
	override fun changeGroup() {
		uiState.showMode = if(uiState.showMode == 0) 1 else 0
		if(uiState.showMode == 1 && showModeGroup[1].isEmpty() && showModeGroup[0].isNotEmpty()) {
			request({
				sortByTerm()
				uiState.lessonGroups = showModeGroup[1]
			})
		}else{
			uiState.lessonGroups = showModeGroup[uiState.showMode]
		}
	}

	override fun clickGroup(index: Int){
		val item = uiState.lessonGroups[index]
		if(!item.hasQuery){
			runBackGround {
				item.lessons = if(uiState.showMode == 0){
					AcademicModel.getInfoByGroup(item.groupInfo.group)
				} else {
					AcademicModel.getInfoByTerm(item.groupInfo.group)
				}
				sort(item)
				item.hasQuery = true
				item.isExpand = !item.isExpand
			}
		}else{
			item.isExpand = !item.isExpand
		}
	}

	override fun clickLesson(group: Int, lesson: Int) {
		val item = uiState.lessonGroups[group]
		if(item.lessonMarks[lesson] === Mark.EMPTY_MARK){
			runBackGround {
				val marks = MarkModel.getMarksByKchId(item.lessons[lesson].kchId)
				if(marks.isNotEmpty()){
					item.lessonMarks[lesson] = marks.maxBy { it.time }
				}
				item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
			}
		}else{
			item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
		}
	}

	override fun sortBy(index: Int, type: Int){
		val group = showModeGroup[uiState.showMode][index]
		group.sortBy = type
		sort(group)
	}

	override fun sortType(index: Int){
		val group = showModeGroup[uiState.showMode][index]
		group.sortType = if(group.sortType == 1) -1 else 1
		sort(group)
	}

	override fun queryData() {
		request({
			val pair = AcademicModel.getAcademic(EasAccount)
			AcademicModel.clearInfo()
			AcademicModel.clearGroups()
			AcademicModel.insertGroups(pair.first)
			AcademicModel.insertInfo(pair.second.toList())
			uiState.showMode = 0
			showModeGroup[0] = pair.first.map { AcademicGroupUIState(it) }
			showModeGroup[1] = emptyList()
			uiState.lessonGroups = showModeGroup[0]
		})
	}


	private fun sort(group: AcademicGroupUIState){
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
		val builders: Array<AcademicGroup.Builder> = Array(Strings.ARRAY_TERM_NAME.size){
			AcademicGroup.Builder(group = it, type = Strings.ARRAY_TERM_NAME[it])
		}

		val entranceTime: Int = EasAccount.entranceDate
		if(entranceTime == -1) {
			toastWarning("未设置入学年份")
		}

		for(index in AcademicModel.getGroupsByTerm()){
			if(index.group >= 0 && index.group < builders.size){
				val group = builders[index.group]
				group.totalCounts = index.totalCounts
				group.passedCounts = index.passedCounts
				group.requireCredits = index.requireCredits
				group.obtainedCredits = index.obtainedCredits
			}
		}

		showModeGroup[1] = List(builders.size){ AcademicGroupUIState(builders[it].build()) }
	}
}

class AcademicGroupUIState(val groupInfo: AcademicGroup){
	var sortBy = 0
	var hasQuery = false

	var isExpand by mutableStateOf(false)
	var sortType by mutableIntStateOf(-1)

	val _lessons: MutableState<List<AcademicInfo>> = mutableStateOf(emptyList())

	var isLessonExpand = lessons.map { false }.toMutableStateList()
	var lessonMarks: Array<Mark> = emptyArray()

	var lessons: List<AcademicInfo>
		get() = _lessons.value
		set(value) {
			isLessonExpand = value.map { false }.toMutableStateList()
			lessonMarks = Array(value.size) { Mark.EMPTY_MARK }
			_lessons.value = value
		}

}

class AcademicUIState {
	/**
	 * 展示模式: 0 - 按课程类型，1 - 按修读年份
	 */
	var showMode by mutableIntStateOf(0)
	var lessonGroups by mutableStateOf(listOf<AcademicGroupUIState>())
}

interface AcademicUIEvent {
	fun queryData() { }
	fun changeGroup() { }

	fun clickGroup(index: Int) { }
	fun clickLesson(group: Int, lesson: Int) { }

	fun sortBy(index: Int, type: Int) { }
	fun sortType(index: Int) { }
}