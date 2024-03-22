package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.api.QustApi
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.VpnEncodeUtils
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

class IPassAccount : Account(
	QustApi.VPN_HOST,
	Keys.IPASS_ACCOUNT,
	Keys.IPASS_PASSWORD,
	"ipassCookie",
	"https"
){

	companion object {
		fun getInstance(): IPassAccount {
			return Instance.INSTANCE
		}
	}

	private object Instance{
		val INSTANCE = IPassAccount()
	}
	
	override suspend fun absCheckLogin(): Boolean {
		getNoRedirect(QustApi.VPN_LOGIN).use {
			val code: Int = it.code
			return if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP){
				!it.header("Location")?.contains("login")!! || login()
			}else true
		}
	}

	override suspend fun absLogin(account: String, password: String): Boolean {
		cookieJar.clearCookies()

		var lt: String
		var loginUrl: HttpUrl

		getNoCheck(QustApi.VPN_LOGIN).use {
			val html: String = it.body?.string() ?: throw IOException("无法获取lt")
			lt = CodeUtils.matcher(VpnEncodeUtils.LT_PATTERN, html) ?: throw IOException("无法获取lt")
			loginUrl = it.request.url
		}

		val secret = VpnEncodeUtils.encode(account, password, lt)

		postNoCheck(
			loginUrl.pathSegments.joinToString(separator = "/"),
			FormBody.Builder()
				.add("rsa", secret)
				.add("ul", account.length.toString())
				.add("pl", password.length.toString())
				.add("lt", lt)
				.add("execution", "e1s1")
				.add("_eventId", "submit")
				.build()
		).use {
			val lastResponse: Response = it.priorResponse ?: return false
			return lastResponse.code == HttpURLConnection.HTTP_MOVED_PERM || lastResponse.code == HttpURLConnection.HTTP_MOVED_TEMP
		}
	}
}