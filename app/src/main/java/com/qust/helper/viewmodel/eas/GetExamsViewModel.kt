package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Data.TermName
import com.qust.helper.data.eas.Exam
import com.qust.helper.model.Logger
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
class GetExamsViewModel(application: Application) : BaseEasViewModel(application){

	private val markDataPath: File

	var examData: Array<Array<Exam>>

	var exams: MutableState<Array<Exam>>

	init{
		markDataPath = File(application.filesDir, "exam")
		if(markDataPath.exists()) {
			try {
				FileInputStream(markDataPath).use {
					examData = Json.decodeFromStream<Array<Array<Exam>>>(it)
				}
			}catch(_: Exception){
				examData = Array(TermName.size) { Array(0){ Exam() } }
			}
		}else{
			examData = Array(TermName.size) { Array(0){ Exam() } }
		}
		exams = mutableStateOf(examData[pickYear.value])
	}

	fun queryExams() {
		viewModelScope.launch {
			showDialog("查询中")
			withContext(Dispatchers.IO){
				if(checkLogin()){
					val pair = getYearAndTerm()
					examData[pickYear.intValue] = easAccount.queryExam(pair.first, pair.second)
					exams.value = examData[pickYear.intValue]
					try {
						FileOutputStream(markDataPath).use {
							Json.encodeToStream(examData, it)
						}
					} catch(e: IOException) {
						Logger.e(e)
					}
				}
			}
			clearDialog()
		}
	}
}


