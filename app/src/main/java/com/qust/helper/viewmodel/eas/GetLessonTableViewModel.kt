package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.qust.helper.App
import com.qust.helper.R
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.model.LessonTableModel
import com.qust.helper.ui.common.toastError
import com.qust.helper.ui.common.toastOK
import com.qust.helper.ui.widget.LessonRender
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.LessonTableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileOutputStream

class GetLessonTableViewModel(application: Application): BaseEasViewModel(application) {

	val pickType: MutableState<Int> = mutableIntStateOf(0)

	var termText by mutableStateOf("")
	var termTimeText by mutableStateOf("")

	var needSave by mutableStateOf(false)

	private var lessonTable by mutableStateOf(LessonTable())

	var lessonRender = mutableStateOf(LessonRender().also {
		it.setLessonTable(lessonTable = LessonTable())
	})

	fun queryLesson(lessonTableViewModel: LessonTableViewModel){
		viewModelScope.launch {
			dialogText = "查询中"
			val pair = getYearAndTerm()
			val result: LessonTableQueryResult

			withContext(Dispatchers.IO){
				easAccount.checkLogin()
				result = if(pickType.value == 0) {
					LessonTableModel.queryLessonTable(easAccount = easAccount, pair.first, pair.second)
				} else {
					LessonTableModel.queryClassLessonTable(easAccount = easAccount, pair.first, pair.second)
				}
				val error = result.error
				if(error == null) {
					termText = result.termText
					lessonTable = result.lessonTable
					termTimeText = getApplication<App>().getString(
						R.string.text_query_term_start_time,
						DateUtils.YMD.format(lessonTableViewModel.lessonTable.value.startDay),
						DateUtils.YMD.format(lessonTable.startDay)
					)
					needSave = true
					toastContent.value = toastOK("获取课表成功！")

					lessonRender.value.setLessonTable(lessonTable = lessonTable)
				}else{
					toastContent.value = toastError(error)
				}
				dialogText = ""
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun saveLessonTable(lessonTableViewModel: LessonTableViewModel){
		val dataFile = File(getApplication<App>().filesDir, "lessonTables")
		if(!dataFile.exists()) dataFile.mkdirs()
		val file = File(dataFile, "lessonTable")

		try{
			FileOutputStream(file).use { stream ->
				Json.encodeToStream<LessonTable>(lessonTable, stream)
			}
			needSave = false
			toastContent.value = toastOK("保存成功")
		}catch(_: Exception) {
			toastContent.value = toastError("保存失败")
		}

		lessonTableViewModel.lessonTable.value = lessonTable
		val render = lessonTableViewModel.lessonRender.value
		render.setLessonTable(lessonTable = lessonTable)
		lessonTableViewModel.lessonRender.value = render
	}
}