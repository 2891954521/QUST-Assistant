package com.qust.helper.viewmodel.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.UpdateRepository
import com.qust.helper.model.downloadAndInstallApk
import com.qust.helper.model.getAppVersionName
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK

class UpdateViewModel: RequestViewModel() {

	val version by mutableStateOf(getAppVersionName())

	var apkUrl by mutableStateOf("")
	var updateInfo by mutableStateOf("")
	var versionCode by mutableStateOf("")

	var progress by mutableFloatStateOf(-1F)

	fun checkUpdate(channel: Int){
		request({
			val info = if(channel == 0){
				UpdateRepository.checkVersionFromGit(UpdateRepository.GITEE_UPDATE_URL)
			}else{
				UpdateRepository.checkVersionFromGit(UpdateRepository.GITHUB_UPDATE_URL)
			}

			if(!info.isNewVersion || info.apkUrl.isEmpty()){
				toastOK("暂无新版本")
				apkUrl = ""
				updateInfo = ""
				versionCode = ""
			}else{
				apkUrl = info.apkUrl
				updateInfo = info.message
				versionCode = info.versionName
			}
		})
	}

	fun downloadApk(){
		runBackGround {
			try {
				progress = 0F
				val success = downloadAndInstallApk(apkUrl) { progress = it }
				progress = -1F
				if(!success) toastError("下载失败")
			}catch(e: Exception){
				progress = -1F
				toastError("下载失败: ${e.message}")
			}
		}
	}
}
