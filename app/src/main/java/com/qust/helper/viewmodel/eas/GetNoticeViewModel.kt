package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.eas.Notice
import com.qust.helper.model.Logger
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
class GetNoticeViewModel(application: Application) : BaseEasViewModel(application){

	private val dataPath: File

	var notices: MutableState<Array<Notice>>

	init{
		dataPath = File(application.filesDir, "notice")
		if(dataPath.exists()) {
			try {
				FileInputStream(dataPath).use {
					notices = mutableStateOf(Json.decodeFromStream<Array<Notice>>(it))
				}
			}catch(_: Exception){
				notices = mutableStateOf(emptyArray())
			}
		}else{
			notices = mutableStateOf(emptyArray())
		}
	}

	fun queryNotice(block: () -> Unit) {
		viewModelScope.launch {
			try{
				dialogText = "查询中"
				withContext(Dispatchers.IO){
					easAccount.checkLogin()
					notices.value = easAccount.queryNotice(1, 20)
					try {
						FileOutputStream(dataPath).use {
							Json.encodeToStream(notices.value, it)
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


