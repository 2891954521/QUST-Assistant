package com.qust.helper.next.ui.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.entity.eas.AcademicGroup
import com.qust.helper.next.module.eas.AcademicModel
import com.qust.helper.next.repository.AcademicRepository
import com.qust.helper.next.repository.AccountRepository
import com.qust.helper.next.repository.MarkRepository
import com.qust.helper.next.ui.page.eas.AcademicGroupUIState
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.viewmodel.BaseViewModel


class QueryAcademicViewModel : BaseViewModel() {

	/**
	 * 展示模式: 0 - 按课程类型，1 - 按修读年份
	 */
	var showMode by mutableIntStateOf(0)

	var lessonGroups by mutableStateOf(listOf<AcademicGroupUIState>())

	private val showModeGroup: Array<List<AcademicGroupUIState>> = arrayOf(emptyList(), emptyList())

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		runInBackground {
			try {
				showModeGroup[0] = AcademicRepository.getGroups().map { AcademicGroupUIState(it) }
				lessonGroups = showModeGroup[0]
			} catch(e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun changeGroup() {
		showMode = if(showMode == 0) 1 else 0
		if(showMode == 1 && showModeGroup[1].isEmpty() && showModeGroup[0].isNotEmpty()) {
			loading("加载中") {
				sortByTerm()
				lessonGroups = showModeGroup[1]
			}
		} else {
			lessonGroups = showModeGroup[showMode]
		}
	}

	fun clickGroup(index: Int) {
		val item = lessonGroups[index]
		if(!item.hasQuery) {
			runInBackground {
				item.lessons = if(showMode == 0) {
					AcademicRepository.getInfoByGroup(item.groupInfo.group)
				} else {
					AcademicRepository.getInfoByTerm(item.groupInfo.group)
				}
				sort(item)
				item.hasQuery = true
				item.isExpand = !item.isExpand
			}
		} else {
			item.isExpand = !item.isExpand
		}
	}

	fun clickLesson(group: Int, lesson: Int) {
		val item = lessonGroups[group]
		if(item.lessonMarks[lesson] == null) {
			runInBackground {
				val marks = MarkRepository.getMarksByKchId(item.lessons[lesson].kchId)
				if(marks.isNotEmpty()) {
					item.lessonMarks[lesson] = marks.maxBy { it.time }
				}
				item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
			}
		} else {
			item.isLessonExpand[lesson] = !item.isLessonExpand[lesson]
		}
	}

	fun sortBy(index: Int, type: Int) {
		val group = showModeGroup[showMode][index]
		group.sortBy = type
		sort(group)
	}

	fun sortType(index: Int) {
		val group = showModeGroup[showMode][index]
		group.sortType = if(group.sortType == 1) -1 else 1
		sort(group)
	}

	fun queryData() {
		loading("查询中") {
			val pair = AcademicModel.getAcademic()
			AcademicRepository.clearInfo()
			AcademicRepository.clearGroups()
			AcademicRepository.insertGroups(pair.first)
			AcademicRepository.insertInfo(pair.second)

			showMode = 0
			showModeGroup[0] = pair.first.map { AcademicGroupUIState(it) }
			showModeGroup[1] = emptyList()
			lessonGroups = showModeGroup[0]
			toastSuccess("查询完成")
		}
	}

	private fun sort(group: AcademicGroupUIState) {
		group.lessons = when(group.sortBy) {
			0 -> group.lessons.sortedWith { a, b -> a.mark.compareTo(b.mark) * group.sortType }
			1 -> group.lessons.sortedWith { a, b ->
				val v = a.credit.compareTo(b.credit) * group.sortType
				if(v == 0) a.mark.compareTo(b.mark) * group.sortType else v
			}
			2 -> group.lessons.sortedWith { a, b ->
				val v = a.status.compareTo(b.status) * group.sortType
				if(v == 0) a.mark.compareTo(b.mark) * group.sortType else v
			}
			else -> group.lessons
		}
	}

	/**
	 * 按修读学期分组
	 */
	private suspend fun sortByTerm() {
		val builders: Array<AcademicGroup.Builder> = Array(Strings.ARRAY_TERM_NAME.size) {
			AcademicGroup.Builder(group = it, type = Strings.ARRAY_TERM_NAME[it])
		}

		val entranceTime = AccountRepository.entranceDate
		if(entranceTime == -1) {
			toastWarning("未设置入学年份")
		}

		for(index in AcademicRepository.getGroupsByTerm()) {
			if(index.group in builders.indices) {
				val group = builders[index.group]
				group.totalCounts = index.totalCounts
				group.passedCounts = index.passedCounts
				group.requireCredits = index.requireCredits
				group.obtainedCredits = index.obtainedCredits
			}
		}

		showModeGroup[1] = List(builders.size) { AcademicGroupUIState(builders[it].build()) }
	}
}