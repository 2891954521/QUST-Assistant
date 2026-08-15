package com.qust.helper.next.network.client

import com.qust.helper.next.App
import com.qust.helper.next.common.exception.NeedLoginException
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.network.api.QustApi
import com.qust.helper.next.network.client.base.BaseAppHttpClient
import com.qust.helper.next.network.client.token.AppCookiesStorage
import com.qust.helper.next.network.entity.login.EasPublicKey
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.util.reflect.TypeInfo
import java.io.IOException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.text.clear

object EasHttpClient: BaseAppHttpClient() {

	override var baseUrl = QustApi.EA_HOSTS[0]

	private val clientNoRedirect by lazy { createHttpClient { followRedirects = false } }

	private val cookieStorage = AppCookiesStorage(DataKeys.EAS_TOKEN)

	override fun buildClient(config: HttpClientConfig<*>) {
		super.buildClient(config)

		config.install(HttpCookies) {
			storage = cookieStorage
		}
	}

	override suspend fun <T> execute(builder: HttpRequestBuilder, type: TypeInfo): T {
		val request = buildRequest(builder)
		val response = client.request(request)

		return if(response.status == HttpStatusCode.Found){
			if(response.headers["location"]?.contains(QustApi.EA_LOGIN) == true){
				if(login()){
					client.request(request).body(type)
				}else{
					throw NeedLoginException("请先登录")
				}
			}else{
				throw NeedLoginException("请先登录")
			}
		}else{
			response.body(type)
		}
	}

	/**
	 * 登录，使用储存的用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(): Boolean {
		return login(AppSetting[DataKeys.EAS_ACCOUNT, ""], AppSetting[DataKeys.EAS_PASSWORD, ""], saveData = false)
	}

	/**
	 * 登录
	 * @param saveData 是否保存用户名密码
	 * @return 是否登录成功
	 */
	suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		val isLogin = if(account.isNullOrEmpty() || password.isNullOrEmpty()) false else baseLogin(account, password)
		if(saveData && isLogin){
			AppSetting[DataKeys.EAS_ACCOUNT] = account
			AppSetting[DataKeys.EAS_PASSWORD] = password
		}
		return isLogin
	}

	/** 退出登录 */
	suspend fun logout() {
		cookieStorage.clear()
		AppSetting.remove(DataKeys.EAS_ACCOUNT)
		AppSetting.remove(DataKeys.EAS_PASSWORD)
	}


	private suspend fun baseLogin(account: String, password: String): Boolean {
		cookieStorage.clear()

		val html = client.get(QustApi.EA_LOGIN).bodyAsText()

		var token: String? = null
		val matcher = "<input (.*?)>".toPattern().matcher(html)
		while(matcher.find()) {
			val input = matcher.group()
			if(input.contains("csrftoken")) {
				token = "value=\"(.*?)\"".toRegex().find(input)?.groups[1]?.value ?: throw IOException("无法获取 csrfToken")
				break
			}
		}
		val csrfToken = token ?: throw IOException("无法获取 csrfToken")

		val publicKey: EasPublicKey
		try {
			publicKey = client.get(QustApi.EA_LOGIN_PUBLIC_KEY).body()
		} catch(e: Exception) {
			e.printStackTrace()
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
			val decoded = Base64.decode(publicKey)
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
			Base64.encode(cipher.doFinal(str.toByteArray()))
		} catch(e: Exception) {
			null
		}
	}
}