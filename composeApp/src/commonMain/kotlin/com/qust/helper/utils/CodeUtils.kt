package com.qust.helper.utils

object CodeUtils {

	val chars = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

	fun byteToHexString(byteArray: ByteArray): String {
		val hexString = StringBuilder()
		for(b in byteArray) {
			val byte = b.toInt() and 0xFF
			hexString.append(chars[byte shr 4])
			hexString.append(chars[byte and 0xF])
		}
		return hexString.toString()
	}

	fun matcher(pattern: Regex, string: String, index: Int = 1): String? {
		val it = pattern.find(string) ?: return null
		return it.groups[index]?.value
	}
}
