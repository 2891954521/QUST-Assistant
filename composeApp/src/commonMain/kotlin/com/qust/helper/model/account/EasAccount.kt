package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.QustApi
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.vo.EasPublicKey
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.model.network.httpClient
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.Logger
import com.qust.helper.utils.SettingUtils
import com.qust.helper.utils.VpnEncodeUtils
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.IOException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Month
import javax.crypto.Cipher

/**
 * 教务系统账号
 */
object EasAccount: Account(
	URLProtocol.HTTPS,
	QustApi.EA_HOSTS[SettingUtils[Keys.EA_HOST, 0].coerceIn(0, QustApi.EA_HOSTS.size - 1)],
	accountKey = Keys.EAS_ACCOUNT,
	passwordKey = Keys.EAS_PASSWORD,
	cookieKey = "easCookie"
) {
	/**
	 * 当前教务节点索引
	 */
	val currentHostIndex: Int = SettingUtils[Keys.EA_HOST, 0].coerceIn(0, QustApi.EA_HOSTS.size - 1)

	/**
	 * 切换教务节点
	 */
	fun changeHost(index: Int) {
		if(index < 0 || index >= QustApi.EA_HOSTS.size) return
		SettingUtils[Keys.EA_HOST] = index
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
		install(DefaultRequest) {
			url {
				val useVpn = SettingUtils[Keys.EA_USE_VPN, false]
				protocol = URLProtocol.HTTPS
				if(useVpn) {
					host = QustApi.VPN_HOST
					port = 443
					val eaHost = QustApi.EA_HOSTS[SettingUtils[Keys.EA_HOST, 0].coerceIn(0, QustApi.EA_HOSTS.size - 1)]
					val full = VpnEncodeUtils.encryptUrl("https", eaHost, "/")
					val encHost = full.removePrefix("https").removeSuffix("/")
					encodedPathSegments = listOf(encHost.trimStart('/')) + encodedPathSegments
				} else {
					host = QustApi.EA_HOSTS[SettingUtils[Keys.EA_HOST, 0].coerceIn(0, QustApi.EA_HOSTS.size - 1)]
					port = 443
				}
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
	 * 入学年份
	 * eg. 2020
	 */
	var entranceDate: Int = SettingUtils[Keys.ENTRANCE_TIME, -1]
		set(date) {
			SettingUtils[Keys.ENTRANCE_TIME] = date
			field = date
		}

	/**
	 * 获取当前年级
	 */
	fun getCurrentGrade(): Int {
		val current: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
		val y = current.year
		return if(y < entranceDate) {
			0
		} else {
			((y - entranceDate) * 2 - if(current.month < Month.AUGUST) 1 else 0).coerceAtMost(Strings.ARRAY_TERM_NAME.size - 1)
		}
	}

	override suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		val response = HttpStatement(request, client).execute()
		if(response.status == HttpStatusCode.Found){
			if(response.headers["location"]?.contains(QustApi.EA_LOGIN) == true){
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

		val html = client.get(QustApi.EA_LOGIN).bodyAsText()

		var token: String? = null
		val matcher = "<input (.*?)>".toPattern().matcher(html)
		while(matcher.find()) {
			val input = matcher.group()
			if(input.contains("csrftoken")) {
				token = CodeUtils.matcher("value=\"(.*?)\"".toRegex(), input) ?: throw IOException("无法获取 csrfToken")
				break
			}
		}
		val csrfToken = token ?: throw IOException("无法获取 csrfToken")

		val publicKey: EasPublicKey
		try {
			publicKey = client.get(QustApi.EA_LOGIN_PUBLIC_KEY).body()
		} catch(e: Exception) {
			Logger.e(e = e)
			throw IOException("无法获取 publicKey", e)
		}

		val rsaPassword = encrypt(password, publicKey.modulus) ?: throw IOException("RSA加密出错")

		val response = clientNoRedirect.post(QustApi.EA_LOGIN){
			method = HttpMethod.Post
			setBody(FormDataContent(parameters {
				append("csrftoken", csrfToken)
				append("yhm", account)
				append("mm", rsaPassword)
			}))
		}

		val code: Int = response.status.value
		return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307
	}


	/**
	 * RSA公钥加密
	 */
	private fun encrypt(str: String, publicKey: String): String? {
		return try {
			// base64编码的公钥
			val decoded = publicKey.decodeBase64Bytes()
			val sb = StringBuilder()
			for(b in decoded) {
				val hex = Integer.toHexString(b.toInt() and 0xFF)
				if(hex.length < 2) sb.append(0)
				sb.append(hex)
			}
			val a = BigInteger(sb.toString(), 16)
			val b = BigInteger("65537")
			val pubKey = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(a, b)) as RSAPublicKey
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			cipher.init(Cipher.ENCRYPT_MODE, pubKey)
			cipher.doFinal(str.toByteArray()).encodeBase64()
		} catch(e: Exception) {
			null
		}
	}

}