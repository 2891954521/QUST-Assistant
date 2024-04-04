package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.qust.helper.App
import com.qust.helper.R
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.common.toastError
import com.qust.helper.ui.common.toastOK
import com.qust.helper.ui.widget.LessonRender
import com.qust.helper.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetLessonTableViewModel(application: Application): BaseEasViewModel(application) {

	val pickType = mutableIntStateOf(0)

	var termText by mutableStateOf("")
	var termTimeText by mutableStateOf("")

	var needSave by mutableStateOf(false)

	private val startDay = mutableStateOf(LessonTableRepository.startDay)
	private val totalWeek = mutableIntStateOf(LessonTableRepository.totalWeek)
	private val lessonTable = mutableStateOf(LessonTable())

	var lessonRender = LessonRender(startDay = startDay, totalWeek = totalWeek, lessonTable = lessonTable)

	fun queryLesson(){
		viewModelScope.launch {
			dialogText = "查询中"
			val pair = getYearAndTerm()
			val result: LessonTableQueryResult

			withContext(Dispatchers.IO){
				easAccount.checkLogin()
				result = if(pickType.intValue == 0) {
					LessonTableRepository.queryLessonTable(easAccount = easAccount, pair.first, pair.second)
				} else {
					LessonTableRepository.queryClassLessonTable(easAccount = easAccount, pair.first, pair.second)
				}
				val error = result.error
				if(error == null) {
					termText = result.termText
					termTimeText = getApplication<App>().getString(
						R.string.text_query_term_start_time,
						DateUtils.YMD.format(LessonTableRepository.startDay),
						DateUtils.YMD.format(result.lessonTable.startDay)
					)
					needSave = true
					toastContent.value = toastOK("获取课表成功！")

					lessonTable.value = result.lessonTable
					startDay.value = result.lessonTable.startDay
					totalWeek.intValue = result.lessonTable.totalWeek
				}else{
					toastContent.value = toastError(error)
				}
				dialogText = ""
			}
		}
	}

	fun saveLessonTable(){
		if(LessonTableRepository.saveLessonTable(lessonRender.lessonTable)){
			toastOK("保存成功")
		}else{
			toastError("保存失败")
		}
	}
}