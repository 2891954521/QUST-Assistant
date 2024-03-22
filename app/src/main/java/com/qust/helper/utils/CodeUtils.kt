package com.qust.helper.utils

import java.util.regex.Pattern

object CodeUtils {

	fun byteToHexString(byteArray: ByteArray): String {
		val hexString = StringBuilder()
		for(b in byteArray) {
			val hex = Integer.toHexString(0xFF and b.toInt())
			if(hex.length == 1) hexString.append('0')
			hexString.append(hex)
		}
		return hexString.toString()
	}

	fun matcher(pattern: Pattern, string: String, index: Int = 1): String?{
		val matcher = pattern.matcher(string)
		return if(matcher.find()) matcher.group(1) else null
	}
}
