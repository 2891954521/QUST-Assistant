package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.LessonQuery
import com.qust.helper.model.eas.LessonTableQueryResult
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableInfo
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import com.qust.helper.viewmodel.extend.toastWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class QueryLessonViewModel: BaseEasViewModel() {

	val lessonTableInfo = MutableStateFlow(LessonTableInfo(
		timeTable = TimeTable.Companion.DEFAULT,
		startDay = SettingModel.startDay,
		currentWeek = 0,
		totalWeek = 1
	))

	val lessonUIState = LessonTableUIState(lessonTableInfo)

	var termText by mutableStateOf("")
	var termTimeText by mutableStateOf("")

	var needSave by mutableStateOf(false)

	val pickType = mutableIntStateOf(0)

	var queryResult: LessonTableQueryResult? = null

	fun queryLesson(){
		request({
			val pair = getPickTerm()

			val result = LessonQuery.queryLessonTable(easAccount = EasAccount, pair.first, pair.second)

			val error = result.error
			if(error != null) {
				toastError(error)
				return@request
			}

			needSave = true

			termText = result.termText

			termTimeText = Strings.MSG_QUERY_TERM_START_TIME.format(
				DateUtils.YMD.format(lessonTableInfo.value.startDay),
				DateUtils.YMD.format(result.startDay)
			)

			val lessons = result.lessons
			if(lessons != null){
				lessonTableInfo.update { it.copy(
					totalWeek = result.totalWeek,
					lessons = lessons
				) }
			}

			queryResult = result
			needSave = true

			toastOK("获取课表成功！")
		})
	}

	fun saveLesson(){
		val result = queryResult

		if(result == null) {
			toastWarning("请先查询课表")
			return
		}

		request({
			LessonTableRepository.saveLessonTable(result)
		}, {
			toastError("保存课表失败")
		})
	}
}