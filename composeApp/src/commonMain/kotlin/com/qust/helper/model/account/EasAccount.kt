package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.QustApi
import com.qust.helper.entity.vo.EasPublicKey
import com.qust.helper.utils.CodeUtils
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.parameters
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import java.io.IOException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * 教务系统账号
 */
object EasAccount: Account(
	URLProtocol.HTTPS,
	QustApi.EA_HOSTS[0],
	accountKey = Keys.EAS_ACCOUNT,
	passwordKey = Keys.EAS_PASSWORD,
	cookieKey = "easCookie"
) {

	override suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		return HttpStatement(request, client).execute()
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