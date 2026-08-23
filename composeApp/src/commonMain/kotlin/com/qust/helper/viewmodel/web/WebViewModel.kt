package com.qust.helper.viewmodel.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.model.account.Account
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastWarning
import io.ktor.http.Url

/**
 * 网页浏览 ViewModel
 * 管理登录状态、Cookie、加载进度与起始 URL
 */
open class WebViewModel(
	private val account: Account,
	val startUrl: String,
) : RequestViewModel() {

	var isLogin by mutableStateOf(false)
		private set

	var hasCheck by mutableStateOf(false)
		private set

	var needLogin by mutableStateOf(false)
		private set

	var progress by mutableFloatStateOf(0f)

	var cookies by mutableStateOf<List<String>>(emptyList())
		private set

	/**
	 * 检查账号登录状态并准备 Cookie
	 */
	fun checkAccountLogin() {
		if (hasCheck) return
		hasCheck = true

		if (account.getAccountName().isEmpty()) {
			needLogin = true
			toastWarning("请先登录")
			return
		}

		request({
			var cookieList = readCookies()
			if (cookieList.isEmpty()) {
				val success = account.login()
				cookieList = readCookies()
				if (!success) {
					needLogin = true
					toastWarning("登录已过期，请重新登录")
					return@request
				}
			}
			cookies = cookieList.map { "${it.name}=${it.value}" }
			isLogin = true
		}, onError = {
			if (it is NeedLoginException) {
				needLogin = true
				toastWarning("请先登录")
			} else {
				toastError("网络错误: ${it.message}")
			}
		})
	}

	private suspend fun readCookies() =
		account.cookieStorage.get(Url("${account.protocol.name.lowercase()}://${account.host}/"))

	fun updateProgress(value: Float) {
		progress = value
	}

	/**
	 * 重置登录检查，用于从登录页返回后重新检查
	 */
	fun resetCheck() {
		hasCheck = false
		isLogin = false
		needLogin = false
		progress = 0f
		cookies = emptyList()
	}
}
