package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.i18n.Strings
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.LessonQuery
import com.qust.helper.model.eas.LessonTableQueryResult
import com.qust.helper.model.lessonTable.LessonTableModel
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import com.qust.helper.viewmodel.extend.toastWarning

class QueryLessonViewModel: BaseEasViewModel() {

	val lessonUIState = LessonTableUIState()

	var termText by mutableStateOf("")
	var termTimeText by mutableStateOf("")
	var totalWeek by mutableIntStateOf(LessonTableModel.totalWeek)

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
			totalWeek = result.totalWeek

			termTimeText = Strings.MSG_QUERY_TERM_START_TIME.format(
				DateUtils.YMD.format(LessonTableModel.startDay),
				DateUtils.YMD.format(result.startDay)
			)

			val lessons = result.lessons
			if(lessons != null) lessonUIState.setLessonTable(lessons)

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
			LessonTableModel.saveLessonTable(result)
		}, {
			toastError("保存课表失败")
		})
	}
}