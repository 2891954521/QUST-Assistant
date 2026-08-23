package com.qust.helper.model

import com.qust.helper.utils.DateUtils

actual fun getAppVersionName(): String = "desktop"

actual fun getPackageBuildDate(): String = DateUtils.YMD.format(DateUtils.today())

actual fun downloadAndInstallApk(url: String, onProgress: (Float) -> Unit): Boolean = false
