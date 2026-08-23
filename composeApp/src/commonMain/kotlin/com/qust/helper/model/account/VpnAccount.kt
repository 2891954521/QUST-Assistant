package com.qust.helper.model.account

import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.utils.Logger
import com.qust.helper.utils.VpnEncodeUtils
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import java.net.HttpURLConnection

/**
 * 走 VPN 通道访问校园网服务的账号包装器。
 *
 * 将 [host] 下的任意请求 URL 通过 [VpnEncodeUtils.encryptUrl] 转换为 VPN 格式，
 * 复用 [IPassAccount] 的登录态与 Cookie 发出请求。
 */
class VpnAccount(
	private val host: String,
	private val scheme: String = "https",
	private val ipass: IPassAccount = IPassAccount,
) {

	var needLogin = true

	val isLogin: Boolean
		get() = ipass.getAccountName().isNotEmpty()

	fun getAccount(): String = ipass.getAccountName()

	/**
	 * 检查 VPN 登录态，若未登录则尝试自动登录
	 */
	@Throws(NeedLoginException::class)
	suspend fun checkLogin(): Boolean {
		return try {
			val response = executeNoRedirect(HttpMethod.Get, "")
			val code = response.status.value
			if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {
				val location = response.headers["Location"] ?: ""
				if(!location.contains("login")) {
					if(needLogin) {
						afterLogin()
					} else {
						true
					}
				} else {
					login()
				}
			} else if(needLogin) {
				afterLogin()
			} else {
				true
			}
		} catch(e: NeedLoginException) {
			throw e
		} catch(e: Exception) {
			Logger.e(e = e)
			false
		}
	}

	open suspend fun afterLogin(): Boolean = true

	@Throws(NeedLoginException::class)
	suspend fun login(): Boolean {
		if(!ipass.login()) {
			needLogin = true
			throw NeedLoginException()
		} else {
			needLogin = false
		}
		return afterLogin()
	}

	@Throws(NeedLoginException::class)
	suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		if(!ipass.login(account, password, saveData)) {
			needLogin = true
			throw NeedLoginException()
		} else {
			needLogin = false
		}
		return afterLogin()
	}

	/**
	 * GET 请求，返回原始响应（带登录态检查）
	 */
	@Throws(NeedLoginException::class)
	suspend fun getOriginal(url: String): HttpResponse {
		val request = createRequest(HttpMethod.Get, url)
		return execute(request)
	}

	/**
	 * POST 表单请求，返回原始响应（带登录态检查）
	 */
	@Throws(NeedLoginException::class)
	suspend fun postOriginal(url: String, body: Map<String, String>): HttpResponse {
		val request = createRequest(HttpMethod.Post, url)
		request.setBody(FormDataContent(parameters {
			body.forEach { (k, v) -> append(k, v) }
		}))
		return execute(request)
	}

	/**
	 * GET 请求，不检查登录态（路径基于本 host）
	 */
	suspend fun getNoCheck(url: String): HttpResponse {
		val request = createRequest(HttpMethod.Get, url)
		return HttpStatement(request, ipass.client).execute()
	}

	/**
	 * POST 表单请求，不检查登录态（路径基于本 host）
	 */
	suspend fun postNoCheck(url: String, body: Map<String, String>): HttpResponse {
		val request = createRequest(HttpMethod.Post, url)
		request.setBody(FormDataContent(parameters {
			body.forEach { (k, v) -> append(k, v) }
		}))
		return HttpStatement(request, ipass.client).execute()
	}

	/**
	 * 带完整 URL 的 GET，不检查登录态（URL 直接加密为 VPN 格式）
	 */
	suspend fun getFullUrlNoCheck(fullUrl: String): HttpResponse {
		val vpnUrl = VpnEncodeUtils.encryptUrl(fullUrl)
		val request = Account.createRequestBuilder(HttpMethod.Get, vpnUrl)
		return HttpStatement(request, ipass.client).execute()
	}

	/**
	 * 带完整 URL 的 POST 表单，不检查登录态
	 */
	suspend fun postFullUrlNoCheck(fullUrl: String, body: Map<String, String>): HttpResponse {
		val vpnUrl = VpnEncodeUtils.encryptUrl(fullUrl)
		val request = Account.createRequestBuilder(HttpMethod.Post, vpnUrl)
		request.setBody(FormDataContent(parameters {
			body.forEach { (k, v) -> append(k, v) }
		}))
		return HttpStatement(request, ipass.client).execute()
	}

	/**
	 * 带完整 URL 的 GET，不检查登录态，不跟随重定向
	 */
	suspend fun getFullUrlNoRedirect(fullUrl: String): HttpResponse {
		val vpnUrl = VpnEncodeUtils.encryptUrl(fullUrl)
		val request = Account.createRequestBuilder(HttpMethod.Get, vpnUrl)
		return HttpStatement(request, ipass.clientNoRedirect).execute()
	}

	/**
	 * 构造请求，把目标 URL 转为 VPN 格式后交由 IPassAccount 发送
	 */
	private fun createRequest(method: HttpMethod, url: String): HttpRequestBuilder {
		val vpnUrl = VpnEncodeUtils.encryptUrl(scheme, host, url)
		return Account.createRequestBuilder(method, vpnUrl)
	}

	/**
	 * 不跟随重定向的执行，用于检查登录态
	 */
	private suspend fun executeNoRedirect(method: HttpMethod, url: String): HttpResponse {
		val request = createRequest(method, url)
		return HttpStatement(request, ipass.clientNoRedirect).execute()
	}

	/**
	 * 带登录态检查的执行
	 */
	private suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		val response = HttpStatement(request, ipass.client).execute()
		if(response.status == HttpStatusCode.MovedPermanently || response.status == HttpStatusCode.Found) {
			if(response.headers["Location"]?.contains("login") == true) {
				if(login()) {
					return HttpStatement(request, ipass.client).execute()
				} else {
					throw NeedLoginException()
				}
			} else {
				throw NeedLoginException()
			}
		}
		return response
	}

	suspend fun logout() {
		ipass.logout()
	}
}
