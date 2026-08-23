package com.qust.helper.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.qust.helper.App
import com.qust.helper.BuildConfig
import com.qust.helper.utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

actual fun getAppVersionName(): String {
	return try {
		App.instance.packageManager.getPackageInfo(App.instance.packageName, 0).versionName ?: ""
	} catch(e: Exception) {
		""
	}
}

actual fun getPackageBuildDate(): String = BuildConfig.PACKAGE_TIME

actual fun downloadAndInstallApk(url: String, onProgress: (Float) -> Unit): Boolean {
	val context = App.instance
	val file = File(context.externalCacheDir, "release.apk")
	try {
		val con = (URL(url).openConnection() as HttpURLConnection).also {
			it.requestMethod = "GET"
			it.readTimeout = 5000
			it.connectTimeout = 5000
		}
		con.connect()
		when(con.responseCode) {
			200 -> {
				val inputStream = con.inputStream
				if(inputStream == null){
					Logger.e("网络错误")
					return false
				}
				var pass = 0
				val len = con.contentLength.toFloat().coerceAtLeast(1F)
				FileOutputStream(file).use { outputStream ->
					val buf = ByteArray(2048)
					var ch: Int
					while(inputStream.read(buf).also { ch = it } != -1) {
						outputStream.write(buf, 0, ch)
						pass += 2048
						onProgress(pass / len)
					}
				}
				installApk(context, file)
				return true
			}
			404 -> {
				Logger.e("新版本文件不存在")
				return false
			}
			else -> {
				Logger.e("连接服务器失败: HTTP ${con.responseCode}")
				return false
			}
		}
	} catch(e: Exception) {
		Logger.e(e = e)
		return false
	}
}

private fun installApk(context: Context, file: File) {
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
