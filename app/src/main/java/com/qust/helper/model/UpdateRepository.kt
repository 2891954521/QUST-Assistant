package com.qust.helper.model

import com.qust.helper.BuildConfig
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.utils.DateUtils
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * 检查更新的工具类
 */
object UpdateRepository {

	const val GITHUB_UPDATE_URL = "https://api.github.com/repos/2891954521/QUST-Assistant/releases/latest"

	const val GITEE_UPDATE_URL = "https://gitee.com/api/v5/repos/berdb/QUST-Assistant/releases/latest"

	/**
	 * 异步检查更新
	 */
	suspend fun checkUpdate(): UpdateInfo? {
		if(!Setting.getBoolean(Keys.KEY_AUTO_UPDATE, true)) return null

		val current = System.currentTimeMillis()
		val frequency = 1000L * 60L * 60L * 24L * 3L
		if((current - Setting.getLong(Keys.LAST_UPDATE_TIME)) < frequency) return null

		Setting.edit { it.putLong(Keys.LAST_UPDATE_TIME, current) }

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
	 * @return 版本信息，没有新版本时返还 null
	 */
	suspend fun checkVersionFromGit(url: String): UpdateInfo {
		try {
			OkHttpClient().newCall(Request.Builder().url(url.toHttpUrl()).build()).execute().use { response ->
				val json = JSONObject(response.body!!.string())

				val buildDate = DateUtils.YMD.parse(BuildConfig.PACKAGE_TIME) ?: return UpdateInfo()
				val publishDate = DateUtils.YMD.parse(json.getString("created_at")) ?: return UpdateInfo()

				val assets = json.getJSONArray("assets")
				if(assets.length() == 0){
					return UpdateInfo()
				}else{
					return UpdateInfo(
						isNewVersion = publishDate.after(buildDate),
						versionName = json.getString("tag_name"),
						message = json.getString("body"),
						apkUrl = assets.getJSONObject(0).getString("browser_download_url")
					)
				}
			}
		} catch(e: Exception) {
			Logger.e(e)
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
