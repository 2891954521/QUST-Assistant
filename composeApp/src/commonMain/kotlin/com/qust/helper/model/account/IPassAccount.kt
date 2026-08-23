package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.QustApi
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.model.network.httpClient
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.SettingUtils
import com.qust.helper.utils.VpnEncodeUtils
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection

object IPassAccount: Account(
	protocol =  URLProtocol. HTTPS,
	host = QustApi.VPN_HOST,
	accountKey = Keys.IPASS_ACCOUNT,
	passwordKey = Keys.IPASS_PASSWORD,
	cookieKey = "ipassCookie",
){
	val redirectList = mutableListOf<HttpResponse>()

	private fun resolveProtocol(): URLProtocol {
		return if (SettingUtils[Keys.IPASS_USE_HTTPS, true]) URLProtocol.HTTPS else URLProtocol.HTTP
	}

	override val client by lazy {
		httpClient {
			createHttpClient(this)
		}
	}

	override val clientNoRedirect by lazy {
		httpClient {
			followRedirects = false
			createHttpClient(this)
		}
	}

	override fun createHttpClient(config: HttpClientConfig<*>) = with(config){
		val protocol = resolveProtocol()
		install(DefaultRequest) {
			url {
				this.protocol = protocol
				host = this@IPassAccount.host
				port = if (protocol == URLProtocol.HTTPS) 443 else 80
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

	val loginClient = httpClient {
		followRedirects = true
		createHttpClient(this)
		install(createClientPlugin("redirect interceptor") {
			on(Send) { request ->
				val call = proceed(request)
				redirectList.add(call.response)
				return@on call
			}
		})
	}

	override suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		val response = HttpStatement(request, client).execute()
		if(response.status == HttpStatusCode.MovedPermanently || response.status == HttpStatusCode.Found){
			if(response.headers["location"]?.contains("login") == true){
				if(login()){
					return HttpStatement(request, client).execute()
				}else{
					throw NeedLoginException()
				}
			}else{
				throw NeedLoginException()
			}
		}else{
			return response
		}
	}

	override suspend fun baseLogin(account: String, password: String): Boolean {
		cookieStorage.clear()

		val request = createRequestBuilder(HttpMethod.Get, "")
		val response = HttpStatement(request, client).execute()
		val html: String = response.bodyAsText()

		val lt: String = CodeUtils.matcher(VpnEncodeUtils.LT_PATTERN, html) ?: throw IOException("无法获取lt")
		val loginUrl = response.request.url

		val secret = VpnEncodeUtils.encode(account, password, lt)

		val loginRequest = createRequestBuilder(HttpMethod.Post, loginUrl.encodedPath)
		loginRequest.setBody(FormDataContent(parameters {
			append("rsa", secret)
			append("ul", account.length.toString())
			append("pl", password.length.toString())
			append("lt", lt)
			append("execution", "e1s1")
			append("_eventId", "submit")
		}))

		redirectList.clear()
		HttpStatement(loginRequest, loginClient).execute()

		val lastResponse: HttpResponse = redirectList.lastOrNull() ?: return false
		val code = lastResponse.status.value
		return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
	}
}