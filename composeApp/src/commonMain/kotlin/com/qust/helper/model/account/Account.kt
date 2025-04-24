package com.qust.helper.model.account

import com.qust.helper.model.network.AppCookiesStorage
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.model.network.httpClient
import com.qust.helper.utils.SettingUtils
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 基础账号类
 */
open class Account(
	val protocol: URLProtocol = URLProtocol.HTTP,
	val host: String,
	val port: Int = when(protocol) { URLProtocol.HTTP -> 80; URLProtocol.HTTPS -> 443; else -> 80 },

	val accountKey: String,
	val passwordKey: String,
	val cookieKey: String
) {

	open val client by lazy {
		httpClient {
			createHttpClient(this)
		}
	}

	open val clientNoRedirect by lazy {
		httpClient {
			followRedirects = true
			createHttpClient(this)
		}
	}

	open val cookieStorage = AppCookiesStorage(cookieKey)

	open fun createHttpClient(config: HttpClientConfig<*>) = with(config){
		install(DefaultRequest) {
			url {
				protocol = this@Account.protocol
				host = this@Account.host
				port = this@Account.port
			}
		}
		install(HttpCookies) {
			storage = cookieStorage
		}
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
				isLenient = true
			})
		}
	}

	/**
	 * 登录，使用储存的用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(): Boolean {
		return login(
			SettingUtils[accountKey, ""],
			SettingUtils[passwordKey, ""],
			saveData = false
		)
	}

	/**
	 * 登录
	 * @param saveData 是否保存用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		val isLogin = if(account.isNullOrEmpty() || password.isNullOrEmpty()) false else baseLogin(account, password)
		if(saveData && isLogin){
			SettingUtils[accountKey] = account
			SettingUtils[passwordKey] = password
		}
		return isLogin
	}

	/**
	 * GET请求
	 */
	@Throws(NeedLoginException::class)
	suspend inline fun <reified T> get(url: String, noinline block: HttpRequestBuilder.() -> Unit = {}): T {
		val request = createRequestBuilder(HttpMethod.Get, url)
		block(request)
		return execute(request).body()
	}

	/**
	 * POST请求
	 */
	@Throws(NeedLoginException::class)
	suspend inline fun <reified T> post(url: String, noinline block: HttpRequestBuilder.() -> Unit = {}): T {
		val request = createRequestBuilder(HttpMethod.Post, url)
		block(request)
		return execute(request).body()
	}

	/**
	 * GET请求，返还原始响应
	 */
	@Throws(NeedLoginException::class)
	suspend fun getOriginal(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
		val request = createRequestBuilder(HttpMethod.Get, url)
		block(request)
		return execute(request)
	}
	/**
	 * POST请求，返还原始响应
	 */
	@Throws(NeedLoginException::class)
	suspend fun postOriginal(url: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
		val request = createRequestBuilder(HttpMethod.Post, url)
		block(request)
		return execute(request)
	}

	/**
	 * 获取账号
	 */
	open fun getAccountName() = SettingUtils[accountKey, ""]


	/**
	 * 执行HTTP请求
	 * 在此处可以检查是否被定向到登录页，若是则重新登录
	 * 登录失败的情况下抛出 NeedLoginException
	 */
	@Throws(NeedLoginException::class)
	open suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		return client.request(request)
	}

	/**
	 * 账号登录
	 * @param account 账号
	 * @param password 密码
	 * @return 是否登录成功
	 */
	protected open suspend fun baseLogin(account: String, password: String): Boolean = false


	companion object {

		fun createRequestBuilder(method: HttpMethod, url: String): HttpRequestBuilder {
			val request = HttpRequestBuilder()
			request.method = method
			request.url(url)
			return request
		}
	}
}