package com.qust.helper.next.network.client

import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.network.api.QustApi
import com.qust.helper.next.network.client.base.BaseAppHttpClient
import com.qust.helper.next.network.client.token.AppCookiesStorage

object IPassHttpClient: BaseAppHttpClient() {

	override var baseUrl = QustApi.IPASS_HOST

	private val cookieStorage = AppCookiesStorage(DataKeys.IPASS_TOKEN)

	/**
	 * 登录，使用储存的用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(): Boolean {
		return login(AppSetting[DataKeys.IPASS_ACCOUNT, ""], AppSetting[DataKeys.IPASS_PASSWORD, ""], saveData = false)
	}

	/**
	 * 登录
	 * @param saveData 是否保存用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		val isLogin = if(account.isNullOrEmpty() || password.isNullOrEmpty()) false else baseLogin(account, password)
		if(saveData && isLogin){
			AppSetting[DataKeys.IPASS_ACCOUNT] = account
			AppSetting[DataKeys.IPASS_PASSWORD] = password
		}
		return isLogin
	}

	/** 退出登录 */
	suspend fun logout() {
		cookieStorage.clear()
		AppSetting.remove(DataKeys.IPASS_ACCOUNT)
		AppSetting.remove(DataKeys.IPASS_PASSWORD)
	}

	private suspend fun baseLogin(account: String, password: String): Boolean {
		return true
//		cookieStorage.clear()
//
//		val request = createRequestBuilder(HttpMethod.Get, "")
//		val response = HttpStatement(request, client).execute()
//		val html: String = response.bodyAsText()
//
//		val lt: String = CodeUtils.matcher(VpnEncodeUtils.LT_PATTERN, html) ?: throw IOException("无法获取lt")
//		val loginUrl = response.request.url
//
//		val secret = VpnEncodeUtils.encode(account, password, lt)
//
//		val loginRequest = createRequestBuilder(HttpMethod.Post, loginUrl.encodedPath)
//		loginRequest.setBody(FormDataContent(parameters {
//			append("rsa", secret)
//			append("ul", account.length.toString())
//			append("pl", password.length.toString())
//			append("lt", lt)
//			append("execution", "e1s1")
//			append("_eventId", "submit")
//		}))
//
//		redirectList.clear()
//		HttpStatement(loginRequest, loginClient).execute()
//
//		val lastResponse: HttpResponse = redirectList.lastOrNull() ?: return false
//		val code = lastResponse.status.value
//		return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
	}
}