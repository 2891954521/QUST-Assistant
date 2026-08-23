package com.qust.helper.model

import com.qust.helper.data.Keys
import com.qust.helper.model.network.httpClient
import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import com.qust.helper.utils.SettingUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 当前应用版本名
 */
expect fun getAppVersionName(): String

/**
 * 当前应用构建日期，格式 yyyy-MM-dd
 */
expect fun getPackageBuildDate(): String

/**
 * 下载并安装 APK
 * @return 是否下载并成功调用安装
 */
expect fun downloadAndInstallApk(url: String, onProgress: (Float) -> Unit): Boolean

/**
 * 检查更新的工具类
 */
object UpdateRepository {

	const val GITHUB_UPDATE_URL = "https://api.github.com/repos/2891954521/QUST-Assistant/releases/latest"

	const val GITEE_UPDATE_URL = "https://gitee.com/api/v5/repos/berdb/QUST-Assistant/releases/latest"

	private val client: HttpClient by lazy {
		httpClient {
			followRedirects = true
		}
	}

	/**
	 * 异步检查更新
	 */
	suspend fun checkUpdate(): UpdateInfo? {
		if(!SettingUtils.getBoolean(Keys.KEY_AUTO_UPDATE, true)) return null

		val current = System.currentTimeMillis()
		val frequency = 1000L * 60L * 60L * 24L * 3L
		if((current - SettingUtils.getLong(Keys.LAST_UPDATE_TIME)) < frequency) return null

		SettingUtils.putLong(Keys.LAST_UPDATE_TIME, current)

		// 从 Gitee 上检查更新
		var info = checkVersionFromGit(GITEE_UPDATE_URL)

		// 从 Github 上检查更新
		if(!info.isNewVersion && info.apkUrl.isNotEmpty()) {
			info = checkVersionFromGit(GITHUB_UPDATE_URL)
		}

		return if(info.isNewVersion && info.apkUrl.isNotEmpty()) {
			info
		}else{
			null
		}
	}

	/**
	 * 从Git检查版本更新
	 * @return 版本信息，没有新版本时返还 UpdateInfo()
	 */
	suspend fun checkVersionFromGit(url: String): UpdateInfo {
		try {
			val response = client.get(url)
			if(!response.status.isSuccess()) return UpdateInfo()
			val json = JsonUtils.parseString<JsonObject>(response.bodyAsText())

			val buildDate = DateUtils.YMD.parse(getPackageBuildDate())
			val createdAt = json["created_at"]?.jsonPrimitive?.contentOrNull ?: return UpdateInfo()
			val publishDate = DateUtils.YMD.parse(createdAt.take(10))

			val assets = json["assets", JSONArray] ?: return UpdateInfo()
			if(assets.isEmpty()) {
				return UpdateInfo()
			}else{
				return UpdateInfo(
					isNewVersion = publishDate > buildDate,
					versionName = json["tag_name", ""],
					message = json["body", ""],
					apkUrl = assets[0].jsonObject["browser_download_url"]?.jsonPrimitive?.contentOrNull ?: ""
				)
			}
		} catch(e: Exception) {
			Logger.e(e = e)
		}
		return UpdateInfo()
	}

	data class UpdateInfo(
		val isNewVersion: Boolean = false,
		val versionName: String = "",
		val message: String = "",
		val apkUrl: String = "",
	)
}
