package com.qust.helper.utils

import com.qust.helper.utils.CodeUtils.byteToHexString
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object VpnEncodeUtils {

	val LT_PATTERN = Pattern.compile("name=\"lt\" value=\"(LT-\\d+-[a-zA-Z\\d]+-tpass)\"")

	private val HEX_DIGITS = "0123456789ABCDEF".toCharArray()

	private val table = intArrayOf(1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1)

	private val table2 = intArrayOf(
		14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
		41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
	)

	private val table3 = intArrayOf(
		58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
		57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7
	)

	private val table4 = intArrayOf(
		40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
		36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25
	)

	private val tableE = intArrayOf(32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1)

	private val tableP = intArrayOf(16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25)

	private val SBox = arrayOf(
		arrayOf(
			longArrayOf(14L, 4L, 13L, 1L, 2L, 15L, 11L, 8L, 3L, 10L, 6L, 12L, 5L, 9L, 0L, 7),
			longArrayOf(0L, 15L, 7L, 4L, 14L, 2L, 13L, 1L, 10L, 6L, 12L, 11L, 9L, 5L, 3L, 8),
			longArrayOf(4L, 1L, 14L, 8L, 13L, 6L, 2L, 11L, 15L, 12L, 9L, 7L, 3L, 10L, 5L, 0),
			longArrayOf(15L, 12L, 8L, 2L, 4L, 9L, 1L, 7L, 5L, 11L, 3L, 14L, 10L, 0L, 6L, 13)
		),
		arrayOf(
			longArrayOf(15L, 1L, 8L, 14L, 6L, 11L, 3L, 4L, 9L, 7L, 2L, 13L, 12L, 0L, 5L, 10),
			longArrayOf(3L, 13L, 4L, 7L, 15L, 2L, 8L, 14L, 12L, 0L, 1L, 10L, 6L, 9L, 11L, 5),
			longArrayOf(0L, 14L, 7L, 11L, 10L, 4L, 13L, 1L, 5L, 8L, 12L, 6L, 9L, 3L, 2L, 15),
			longArrayOf(13L, 8L, 10L, 1L, 3L, 15L, 4L, 2L, 11L, 6L, 7L, 12L, 0L, 5L, 14L, 9)
		),
		arrayOf(
			longArrayOf(10L, 0L, 9L, 14L, 6L, 3L, 15L, 5L, 1L, 13L, 12L, 7L, 11L, 4L, 2L, 8),
			longArrayOf(13L, 7L, 0L, 9L, 3L, 4L, 6L, 10L, 2L, 8L, 5L, 14L, 12L, 11L, 15L, 1),
			longArrayOf(13L, 6L, 4L, 9L, 8L, 15L, 3L, 0L, 11L, 1L, 2L, 12L, 5L, 10L, 14L, 7),
			longArrayOf(1L, 10L, 13L, 0L, 6L, 9L, 8L, 7L, 4L, 15L, 14L, 3L, 11L, 5L, 2L, 12)
		),
		arrayOf(
			longArrayOf(7L, 13L, 14L, 3L, 0L, 6L, 9L, 10L, 1L, 2L, 8L, 5L, 11L, 12L, 4L, 15),
			longArrayOf(13L, 8L, 11L, 5L, 6L, 15L, 0L, 3L, 4L, 7L, 2L, 12L, 1L, 10L, 14L, 9),
			longArrayOf(10L, 6L, 9L, 0L, 12L, 11L, 7L, 13L, 15L, 1L, 3L, 14L, 5L, 2L, 8L, 4),
			longArrayOf(3L, 15L, 0L, 6L, 10L, 1L, 13L, 8L, 9L, 4L, 5L, 11L, 12L, 7L, 2L, 14)
		),
		arrayOf(
			longArrayOf(2L, 12L, 4L, 1L, 7L, 10L, 11L, 6L, 8L, 5L, 3L, 15L, 13L, 0L, 14L, 9),
			longArrayOf(14L, 11L, 2L, 12L, 4L, 7L, 13L, 1L, 5L, 0L, 15L, 10L, 3L, 9L, 8L, 6),
			longArrayOf(4L, 2L, 1L, 11L, 10L, 13L, 7L, 8L, 15L, 9L, 12L, 5L, 6L, 3L, 0L, 14),
			longArrayOf(11L, 8L, 12L, 7L, 1L, 14L, 2L, 13L, 6L, 15L, 0L, 9L, 10L, 4L, 5L, 3)
		),
		arrayOf(
			longArrayOf(12L, 1L, 10L, 15L, 9L, 2L, 6L, 8L, 0L, 13L, 3L, 4L, 14L, 7L, 5L, 11),
			longArrayOf(10L, 15L, 4L, 2L, 7L, 12L, 9L, 5L, 6L, 1L, 13L, 14L, 0L, 11L, 3L, 8),
			longArrayOf(9L, 14L, 15L, 5L, 2L, 8L, 12L, 3L, 7L, 0L, 4L, 10L, 1L, 13L, 11L, 6),
			longArrayOf(4L, 3L, 2L, 12L, 9L, 5L, 15L, 10L, 11L, 14L, 1L, 7L, 6L, 0L, 8L, 13)
		),
		arrayOf(
			longArrayOf(4L, 11L, 2L, 14L, 15L, 0L, 8L, 13L, 3L, 12L, 9L, 7L, 5L, 10L, 6L, 1),
			longArrayOf(13L, 0L, 11L, 7L, 4L, 9L, 1L, 10L, 14L, 3L, 5L, 12L, 2L, 15L, 8L, 6),
			longArrayOf(1L, 4L, 11L, 13L, 12L, 3L, 7L, 14L, 10L, 15L, 6L, 8L, 0L, 5L, 9L, 2),
			longArrayOf(6L, 11L, 13L, 8L, 1L, 4L, 10L, 7L, 9L, 5L, 0L, 15L, 14L, 2L, 3L, 12)
		),
		arrayOf(
			longArrayOf(13L, 2L, 8L, 4L, 6L, 15L, 11L, 1L, 10L, 9L, 3L, 14L, 5L, 0L, 12L, 7),
			longArrayOf(1L, 15L, 13L, 8L, 10L, 3L, 7L, 4L, 12L, 5L, 6L, 11L, 0L, 14L, 9L, 2),
			longArrayOf(7L, 11L, 4L, 1L, 9L, 12L, 14L, 2L, 0L, 6L, 10L, 13L, 15L, 3L, 5L, 8),
			longArrayOf(2L, 1L, 14L, 7L, 4L, 10L, 8L, 13L, 15L, 12L, 9L, 0L, 3L, 5L, 6L, 11)
		)
	)

	private fun Encrypt(msg: String, key1: String, key2: String, key3: String): String {
		val msgByte = str2bytes(msg)
		val key1Byte = str2bytes(key1)
		val key2Byte = str2bytes(key2)
		val key3Byte = str2bytes(key3)
		val sb = StringBuilder()
		var m = 0
		while(m < msgByte.size) {
			var tmpMsg = ByteArray(8)
			System.arraycopy(msgByte, m, tmpMsg, 0, 8)
			run {
				var k = 0
				while(k < key1Byte.size) {
					val tmpKey = ByteArray(8)
					System.arraycopy(key1Byte, k, tmpKey, 0, 8)
					tmpMsg = enc(tmpMsg, tmpKey)
					k += 8
				}
			}
			run {
				var k = 0
				while(k < key2Byte.size) {
					val tmpKey = ByteArray(8)
					System.arraycopy(key2Byte, k, tmpKey, 0, 8)
					tmpMsg = enc(tmpMsg, tmpKey)
					k += 8
				}
			}
			var k = 0
			while(k < key3Byte.size) {
				val tmpKey = ByteArray(8)
				System.arraycopy(key3Byte, k, tmpKey, 0, 8)
				tmpMsg = enc(tmpMsg, tmpKey)
				k += 8
			}
			val hexChars = CharArray(16)
			for(i in 0 .. 7) {
				val value = tmpMsg[i].toInt() and 0xFF
				hexChars[i * 2] = HEX_DIGITS[value ushr 4]
				hexChars[i * 2 + 1] = HEX_DIGITS[value and 0x0F]
			}
			sb.append(hexChars)
			m += 8
		}
		return sb.toString()
	}

	private fun enc(msg: ByteArray, key: ByteArray): ByteArray {
		val keyLong = KeyTo56(byte2long(key))
		var c = keyLong shr 28
		var d = keyLong and 268435455L
		val kList = LongArray(16)
		for(i in 0 .. 15) {
			c = (c shr 28 - table[i]) + (c shl table[i]) and 268435455L
			d = (d shr 28 - table[i]) + (d shl table[i]) and 268435455L
			val t = (c shl 28) + d
			for(j in 0 .. 47) {
				kList[i] += 1L shl 56 - table2[j] and t shr 56 - table2[j] shl 47 - j
			}
		}
		val msgBig = BigInteger(1, msg)
		var n = BigInteger.valueOf(0)
		for(i in 0 .. 63) {
			n = n.add(BigInteger.valueOf(1L shl 64 - table3[i]).and(msgBig).shiftRight(64 - table3[i]).shiftLeft(63 - i))
		}
		var tmp: BigInteger
		var l = n.shiftRight(32)
		var r = n.and(BigInteger.valueOf(0xFFFFFFFFL))
		for(j in 0 .. 15) {
			tmp = l.xor(F(r, BigInteger.valueOf(kList[j])))
			l = r
			r = tmp
		}
		tmp = r.shiftLeft(32).add(l)
		var res = BigInteger.ZERO
		for(i in 0 .. 63) {
			res = res.add(BigInteger.valueOf(1L shl 64 - table4[i]).and(tmp).shiftRight(64 - table4[i]).shiftLeft(63 - i))
		}
		return long2byte(res.toLong())
	}

	private fun KeyTo56(k: Long): Long {
		val keyByte = ByteArray(64)
		for(x in 63 downTo 0) {
			keyByte[63 - x] = (k shr x and 1L).toByte()
		}
		val key = ByteArray(56)
		for(i in 0 .. 6) {
			for(j in 0 .. 7) {
				val kIndex = 7 - j
				key[i * 8 + j] = keyByte[8 * kIndex + i]
			}
		}
		var keyInt: Long = 0
		for(b in key) {
			keyInt = (keyInt shl 1) + b
		}
		return keyInt
	}

	private fun F(r: BigInteger, k: BigInteger): BigInteger {
		var r2 = BigInteger.ZERO
		for(i in 0 .. 47) {
			r2 = r2.add(BigInteger.valueOf(1L shl 32 - tableE[i]).and(r).shiftRight(32 - tableE[i]).shiftLeft(47 - i))
		}
		val r3 = r2.xor(k)
		var r4 = BigInteger.ZERO
		for(i in 0 .. 7) {
			val s = BigInteger.valueOf(63L shl (7 - i) * 6).and(r3).shiftRight((7 - i) * 6).toLong()
			val x = (30L and s shr 1).toInt()
			val y = ((32L and s shr 4) + (1L and s)).toInt()
			r4 = r4.add(BigInteger.valueOf(SBox[i][y][x] shl (7 - i) * 4))
		}
		var r5 = BigInteger.ZERO
		for(i in 0 .. 31) {
			r5 = r5.add(BigInteger.valueOf(1L shl 32 - tableP[i]).and(r4).shiftRight(32 - tableP[i]).shiftLeft(31 - i))
		}
		return r5
	}

	private fun long2byte(value: Long): ByteArray {
		val result = ByteArray(8)
		for(i in 0 .. 7) {
			result[i] = (value ushr (8 - i - 1) * 8 and 0xFFL).toByte()
		}
		return result
	}

	private fun byte2long(bytes: ByteArray): Long {
		var result: Long = 0
		for(b in bytes) {
			result = (result shl 8) + (b.toInt() and 0xFF)
		}
		return result
	}

	private fun str2bytes(string: String): ByteArray {
		val length = string.length * 2
		val paddingLength = (8 - length % 8) % 8
		val bts = ByteArray(length + paddingLength)
		var index = 0
		for(x in string.toCharArray()) {
			bts[index++] = (x.code shr 8).toByte()
			bts[index++] = x.code.toByte()
		}
		return bts
	}

	private var IVHex: String
	private var cipher: Cipher

	init {
		try {
			val KeyBytes = "wrdvpnisthebest!".toByteArray()
			IVHex = byteToHexString(KeyBytes)
			cipher = Cipher.getInstance("AES/CFB/NoPadding")
			cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(KeyBytes, "AES"), IvParameterSpec(KeyBytes))
		} catch(e: NoSuchAlgorithmException) {
			throw RuntimeException(e)
		}
	}

	@Throws(Exception::class)
	private fun encrypt(text: String): String {
		val encryptedBytes = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))
		return IVHex + byteToHexString(encryptedBytes)
	}

	/**
	 * 将只能在校园网访问的URL转换为走VPN的URL
	 * @param originUrl 原始URL
	 * @return VPN URL
	 * @throws Exception
	 */
	fun encryptUrl(originUrl: String): String {
		var url = originUrl
		val protocol: String
		if(url.startsWith("http://")) {
			url = url.substring(7)
			protocol = "http"
		} else if(url.startsWith("https://")) {
			url = url.substring(8)
			protocol = "https"
		} else {
			throw RuntimeException("Not a valid URL")
		}

		// 处理ipv6
		var host: String? = null
		val pattern = Pattern.compile("\\[[0-9a-fA-F:]+?\\]")
		val matcher = pattern.matcher(url)
		if(matcher.find()) {
			host = matcher.group(0)
			url = url.substring(matcher.end())
		}

		// 提取端口
		var port: String? = null
		val parts = url.split("?")[0].split(":")
		if(parts.size > 1) {
			port = parts[1].split("/")[0]
			url = url.substring(0, parts[0].length) + url.substring(parts[0].length + port.length + 1)
		}

		// 只对host进行加密
		val i = url.indexOf('/')
		var path = "/"
		if(i == -1) {
			if(host == null) host = url
		} else {
			if(host == null) host = url.substring(0, i)
			path = url.substring(i)
		}
		return if(port == null) {
			encryptUrl(protocol, host, path)
		} else {
			encryptUrl(protocol, host, path, port)
		}
	}

	fun encryptUrl(protocol: String, host: String, url: String): String {
		return protocol + "/" + encrypt(host) + if(url.startsWith("/")) url else "/$url"
	}

	fun encryptUrl(protocol: String, host: String, url: String, port: String): String {
		return protocol + "-" + port + "/" + encrypt(host) + if(url.startsWith("/")) url else "/$url"
	}

	fun encode(userName: String, password: String, lt: String): String {
		return Encrypt(userName + password + lt, "1", "2", "3")
	}
}
