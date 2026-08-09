package com.qust.helper.next.utils

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64

object CodeUtils {

	val HEX_CODE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

	fun md5(str: String): String{
		val md5 = MessageDigest.getInstance("MD5")
		md5.update(str.toByteArray())
		val data = md5.digest()
		val r = StringBuilder(data.size * 2)
		for(b in data) {
			r.append(HEX_CODE[b.toInt() shr 4 and 0xF])
			r.append(HEX_CODE[b.toInt() and 0xF])
		}
		return r.toString()
	}

	/**
	 * RSA公钥加密
	 */
	fun rsaEncode(str: String, publicKey: String): String? {
		return try {
			val decoded = Base64.decode(publicKey)
			val pubKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decoded))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			cipher.init(Cipher.ENCRYPT_MODE, pubKey)
			Base64.encode(cipher.doFinal(str.toByteArray()))
		} catch(e: Exception) {
			null
		}
	}
}