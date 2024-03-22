package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Data
import com.qust.helper.data.eas.Academic
import com.qust.helper.model.Logger
import com.qust.helper.ui.common.toastError
import com.qust.helper.ui.common.toastWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalSerializationApi::class)
class GetAcademicViewModel(application: Application): BaseEasViewModel(application) {

	private val dataPath: File

	private val showModeGroup: Array<Array<Academic.LessonInfoGroup>> = arrayOf(emptyArray(), emptyArray())

	/**
	 * 展示模式: 0 - 按课程类型，1 - 按修读年份
	 */
	private var showMode = 0

	var choose = -1
	var lessonInfo: Array<Academic.LessonInfo>
	var lessonGroups: MutableState<Array<Academic.LessonInfoGroup>>

	init{
		dataPath = File(application.filesDir, "academic")
		if(!dataPath.exists()) dataPath.mkdirs()

		val groupFile = File(dataPath, "groups")
		val lessonFile = File(dataPath, "lessons")

		if(groupFile.exists() && lessonFile.exists()){
			try {
				FileInputStream(groupFile).use {
					showModeGroup[0] = Json.decodeFromStream<Array<Academic.LessonInfoGroup>>(it)
				}
				FileInputStream(lessonFile).use {
					lessonInfo = Json.decodeFromStream<Array<Academic.LessonInfo>>(it)
				}
			}catch(_: Exception){
				showModeGroup[0] = emptyArray()
				lessonInfo = emptyArray()
			}
		}else{
			showModeGroup[0] = emptyArray()
			lessonInfo = emptyArray()
		}
		lessonGroups = mutableStateOf(showModeGroup[0])
	}

	/**
	 * 切换展示模式
	 */
	fun changeGroupMode() {
		showMode = if(showMode == 0) 1 else 0
		if(showMode == 1 && showModeGroup[1].isEmpty() && showModeGroup[0].isNotEmpty()) {
			sortByTerm()
		}
		lessonGroups.value = showModeGroup[showMode]
	}

	fun sortByMark(index: Int){
		choose = index
		val group = showModeGroup[showMode][index]
		group.lessonIndex = group.lessonIndex.sortedWith { a, b ->
			-lessonInfo[a].mark.compareTo(lessonInfo[b].mark)
		}.toIntArray()
		lessonGroups.value = showModeGroup[showMode].clone()
	}

	fun sortByCredit(index: Int){
		choose = index
		val group = showModeGroup[showMode][index]
		Logger.i("before sort: ${group.lessonIndex.joinToString(", ")}")
		group.lessonIndex = group.lessonIndex.sortedWith { a, b ->
			-lessonInfo[a].credit.compareTo(lessonInfo[b].credit)
		}.toIntArray()
		Logger.i("sorted: ${group.lessonIndex.joinToString(", ")}")
		lessonGroups.value = showModeGroup[showMode].clone()
	}

	/**
	 * 按修读学期分组
	 */
	private fun sortByTerm() {
		val builders: Array<Academic.LessonInfoGroup.Builder> = Array(Data.TermName.size){
			Academic.LessonInfoGroup.Builder(groupName = Data.TermName[it])
		}

		var entranceTime: Int = easAccount.entranceTime
		if(entranceTime == -1) {
			toastContent.value = toastWarning("未设置入学年份")
			entranceTime = 0
		}

		lessonInfo.forEachIndexed { i, lesson ->
			val index = Math.max(0, (lesson.year - entranceTime) * 2 + lesson.term - 1)
			if(index < builders.size) {
				val builder = builders[index]
				builder.addLesson(i)
				val credit = lesson.credit.toFloatOrNull() ?: 0F
				if(lesson.status == 4) builder.obtainedCredits += credit
				builder.requireCredits += credit
			}
		}

		showModeGroup[1] = Array(builders.size){ builders[it].build() }
	}


	fun queryData(block: () -> Unit) {
		viewModelScope.launch {
			try{
				dialogText = "查询中"
				withContext(Dispatchers.IO){
					easAccount.checkLogin()
					val pair = easAccount.getAcademic()
					showModeGroup[0] = pair.first
					showModeGroup[1] = emptyArray()
					try {
						val groupFile = File(dataPath, "groups")
						val lessonFile = File(dataPath, "lessons")
						FileOutputStream(groupFile).use {
							Json.encodeToStream(pair.first, it)
						}
						FileOutputStream(lessonFile).use {
							Json.encodeToStream(pair.second, it)
						}
					} catch(_: IOException) { }
					showMode = 0
					lessonInfo = pair.second
					lessonGroups.value = showModeGroup[0]
				}
				block()
			}catch(e: IOException){
				Logger.e("网络错误", e)
				toastContent.value = toastError("网络错误: " + e.message)
			}finally{
				dialogText = ""
			}
		}
	}
}