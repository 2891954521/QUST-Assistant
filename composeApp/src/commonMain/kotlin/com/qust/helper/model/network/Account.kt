package com.qust.helper.model.network

import com.qust.helper.utils.SettingUtils
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

open class Account(
	val protocol: URLProtocol = URLProtocol.HTTP,
	val host: String,
	val port: Int = when(protocol) { URLProtocol.HTTP -> 80; URLProtocol.HTTPS -> 443; else -> 80 },
	var accountName: String,
	var passwordName: String,
	val cookieName: String
) {

	val client = createHttpClient()

	open fun createHttpClient(): HttpClient {
		return httpClient {
			install(DefaultRequest) {
				url {
					protocol = this@Account.protocol
					host = this@Account.host
					port = this@Account.port
				}
			}
			install(HttpCookies){
				storage = AcceptAllCookiesStorage()
			}
			install(ContentNegotiation) {
				json()
			}
		}
	}


	@Throws(IOException::class, NeedLoginException::class)
	suspend fun login(): Boolean {
		return login(
			SettingUtils[accountName, ""],
			SettingUtils[passwordName, ""],
			saveData = false
		)
	}

	@Throws(IOException::class)
	suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		val isLogin = if(account.isNullOrEmpty() || password.isNullOrEmpty()) false else absLogin(account, password)
		if(saveData && isLogin){
			SettingUtils[accountName] = account
			SettingUtils[passwordName] = password
		}
		return isLogin
	}

	@Throws(IOException::class, NeedLoginException::class)
	suspend inline fun <T> get(url: String): Result<T> = getRaw(url).body()

	@Throws(IOException::class, NeedLoginException::class)
	suspend inline fun <reified T, R> post(url: String, body: T?): Result<R> = postRaw(url, body).body()

	@Throws(IOException::class, NeedLoginException::class)
	suspend inline fun getRaw(url: String): HttpResponse = execute(buildGetRequest<Unit>(url, null))

	@Throws(IOException::class, NeedLoginException::class)
	suspend inline fun <reified T> postRaw(url: String, body: T?) = execute(buildPostRequest(url, body))

	inline fun <reified T> buildGetRequest(url: String, params: T? = null): HttpRequestBuilder {
		val request = HttpRequestBuilder()
		request.method = HttpMethod.Get
		request.url(url)
		return request
	}

	inline fun <reified T> buildPostRequest(url: String, body: T? = null): HttpRequestBuilder {
		val request = HttpRequestBuilder()
		request.method = HttpMethod.Post
		request.url(url)
		request.setBody(body)
		request.contentType(ContentType.Application.Json)
		return request
	}

	@Throws(IOException::class, NeedLoginException::class)
	open suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		return HttpStatement(request, client).execute()
	}

	@Throws(IOException::class)
	protected open suspend fun absLogin(account: String, password: String): Boolean = false
}