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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class QueryLessonViewModel: BaseEasViewModel() {

	val tableUIState = LessonTableUIState()

	var termText by mutableStateOf("")
	var termTimeText by mutableStateOf("")
	var startDay by mutableStateOf(LessonTableModel.startDay)
	var totalWeek by mutableIntStateOf(LessonTableModel.totalWeek)

	var needSave by mutableStateOf(false)

	val pickType = mutableIntStateOf(0)

	fun queryLesson(){
		request({
			val pair = getPickTerm()
			val result: LessonTableQueryResult

			result = LessonQuery.queryLessonTable(easAccount = EasAccount, pair.first, pair.second)

			val error = result.error
			if(error == null) {
				needSave = true

				termText = result.termText
				startDay = result.startDay
				totalWeek = result.totalWeek

				termTimeText = Strings.MSG_QUERY_TERM_START_TIME.format(
					DateUtils.YMD.format(LessonTableModel.startDay.toLocalDateTime(TimeZone.UTC)),
					DateUtils.YMD.format(result.startDay.toLocalDateTime(TimeZone.UTC))
				)

				val lessons = result.lessons
				if(lessons != null) tableUIState.setLessonTable(lessons)

				toastOK("获取课表成功！")
			}else{
				toastError(error)
			}
		})
	}
}