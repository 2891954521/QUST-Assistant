package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.model.Logger
import com.qust.helper.data.Data.TermName
import com.qust.helper.data.eas.Mark
import com.qust.helper.ui.common.toastError
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
class GetMarksViewModel(application: Application) : BaseEasViewModel(application){

	private val markDataPath: File

	var marksData: Array<Array<Mark>>

	var marks: MutableState<Array<Mark>>

	init{
		markDataPath = File(application.filesDir, "mark")
		if(markDataPath.exists()) {
			try {
				FileInputStream(markDataPath).use {
					marksData = Json.decodeFromStream<Array<Array<Mark>>>(it)
				}
			}catch(_: Exception){
				marksData = Array(TermName.size) { Array(0){ Mark() } }
			}
		}else{
			marksData = Array(TermName.size) { Array(0){ Mark() } }
		}
		marks = mutableStateOf(marksData[pickYear.value])
	}

	fun queryMarks( block: ()-> Unit) {

		viewModelScope.launch {
			try{
				dialogText = "查询中"
				withContext(Dispatchers.IO){
					easAccount.checkLogin()
					val pair = getYearAndTerm()
					marksData[pickYear.value] = easAccount.queryMark(pair.first, pair.second)
					marks.value = marksData[pickYear.value]
					try {
						FileOutputStream(markDataPath).use {
							Json.encodeToStream(marksData, it)
						}
					} catch(e: IOException) {
						Logger.e(e)
					}
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


