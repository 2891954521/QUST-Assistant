package com.qust.helper.ui.page.app

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.model.UpdateRepository
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.Dialogs
import com.qust.helper.ui.widget.ListPicker
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

object UpdatePage {
	
	val UpdatePage = Page(Keys.Page.UpdatePage, "检查更新") { activity, padding, _, _ ->
		val viewModel by activity.viewModels<UpdateViewModel>()
		UpdatePageUI(padding, viewModel, activity.toast)
	}

	val updateChannel = listOf("Gitee", "GitHub")

	@Composable
	fun UpdatePageUI(padding: PaddingValues, viewModel: UpdateViewModel, toast: Toast) {
		var channel by remember { mutableIntStateOf(0) }

		Column(
			modifier = Modifier.fillMaxSize().padding(padding),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Image(painter = painterResource(id = R.mipmap.ic_launcher), contentDescription = null)
			Text(text = viewModel.version, Modifier.padding(8.dp))

			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(text = "更新渠道")
				ListPicker.ListItemPicker(value = channel, list = updateChannel.indices.toList(), label = { updateChannel[it] }, onValueChange = { channel = it })
				Button(onClick = { viewModel.checkUpdate(channel) }) {
					Text(text = "检查更新")
				}
			}

			if(viewModel.apkUrl.isNotEmpty()){
				Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
					val uriHandler = LocalUriHandler.current
					val annotatedString = buildAnnotatedString {
						append("下载地址: ")
						pushStringAnnotation(tag = "url", annotation = viewModel.apkUrl)
						withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(viewModel.apkUrl) }
						pop()
					}
					ClickableText(text = annotatedString, onClick = { offset ->
						annotatedString.getStringAnnotations(tag = "url", start = offset, end = offset).firstOrNull()?.let {
							uriHandler.openUri(it.item)
						}
					}, modifier = Modifier.weight(1F))

					Button(onClick = { viewModel.downloadApk() }) {
						Text(text = "更新")
					}
				}
			}

			Text(text = "更新日志: \n${viewModel.versionCode}\n${viewModel.updateInfo}", modifier = Modifier.fillMaxWidth().padding(16.dp))
		}

		AppWidgets.DialogBar(dialogText = viewModel.dialogText)

		if(viewModel.progress >= 0F){
			Dialogs.ProgressDialog("正在下载", viewModel.progress, "${(viewModel.progress * 100).roundToInt()}/100")
		}

		toast.ToastContent(toastContent = viewModel.toastContent)
	}

	class UpdateViewModel(application: Application): BaseAndroidViewModel(application){

		val version: String by mutableStateOf(
			try {
				application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
			} catch(_: Exception) {
				""
			}
		)

		var apkUrl by mutableStateOf("")
		var updateInfo by mutableStateOf("")
		var versionCode by mutableStateOf("")

		var progress by mutableFloatStateOf(-1F)

		var downloadSuccess = false

		fun checkUpdate(channel: Int){
			viewModelScope.launch {
				showDialog(getApplication<Application>().resources.getString(R.string.text_checking_version))
				withContext(Dispatchers.IO){
					val info = if(channel == 0){
						UpdateRepository.checkVersionFromGit(UpdateRepository.GITEE_UPDATE_URL)
					}else{
						UpdateRepository.checkVersionFromGit(UpdateRepository.GITHUB_UPDATE_URL)
					}

					if(!info.isNewVersion || info.apkUrl.isEmpty()) {
						toastOK("暂无新版本")
						apkUrl = ""
						updateInfo = ""
						versionCode = ""
						return@withContext
					}else{
						apkUrl = info.apkUrl
						updateInfo = info.message
						versionCode = info.versionName
					}
				}
				clearDialog()
			}
		}

		fun downloadApk() {
			downloadSuccess = false
			viewModelScope.launch {
				progress = 0F
				val file = File(getApplication<Application>().externalCacheDir, "release.apk")
				withContext(Dispatchers.IO){
					try {
						val con = (URL(apkUrl).openConnection() as HttpURLConnection).also {
							it.requestMethod = "GET"
							it.readTimeout = 5000
							it.connectTimeout = 5000
						}
						con.connect()
						when(con.responseCode) {
							200 -> {
								val inputStream = con.inputStream
								if(inputStream == null){
									toastError("网络错误")
									return@withContext
								}
								var pass = 0
								val len = con.contentLength.toFloat().coerceAtLeast(1F)
								FileOutputStream(file).use { outputStream ->
									val buf = ByteArray(2048)
									var ch: Int
									while(inputStream.read(buf).also { ch = it } != -1) {
										outputStream.write(buf, 0, ch)
										pass += 2048
										progress = pass / len
									}
									downloadSuccess = true
								}
							}
							404 -> {
								toastError("新版本文件不存在")
							}
							else -> {
								toastError("连接服务器失败: HTTP ${con.responseCode}")
							}
						}
					} catch(e: IOException) {
						toastError("下载失败: ${e.message}")
					}
				}
				progress = -1F
				if(downloadSuccess){
					installApk()
				}
			}
		}

		private fun installApk() {
			val context = getApplication<Application>()
			val file = File(context.externalCacheDir, "release.apk")
			val intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			val data: Uri
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				data = FileProvider.getUriForFile(context, context.packageName, file)
				// 给目标应用一个临时授权
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			} else {
				data = Uri.fromFile(file)
			}
			intent.setDataAndType(data, "application/vnd.android.package-archive")
			context.startActivity(intent)
		}
	}
}