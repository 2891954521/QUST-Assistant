package com.qust.helper.utils

import java.io.File
import java.nio.charset.Charset
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

actual class SecureStorage {

    private val file = File(System.getProperty("user.home"), ".qust-helper/secure.dat")
    private val key: Key = SecretKeySpec("qust_helper_desktop_2024_v1.0.0".toByteArray(), "AES")
    private val charset = Charsets.UTF_8

    init {
        file.parentFile?.mkdirs()
    }

    actual fun putString(key: String, value: String) {
        val current = readAll()
        val map = current.toMutableMap()
        map[key] = value
        writeAll(map)
    }

    actual fun getString(key: String, defValue: String): String {
        val current = readAll()
        return current[key] ?: defValue
    }

    actual fun remove(key: String) {
        val current = readAll().toMutableMap()
        current.remove(key)
        if (current.isEmpty()) {
            file.delete()
        } else {
            writeAll(current)
        }
    }

    actual fun contains(key: String): Boolean {
        val current = readAll()
        return current.containsKey(key)
    }

    private fun readAll(): Map<String, String> {
        if (!file.exists()) return emptyMap()
        return try {
            val encrypted = file.readText(charset)
            val decrypted = decrypt(encrypted)
            decrypted.split("&").associate { entry ->
                val idx = entry.indexOf('=')
                if (idx > 0) {
                    entry.substring(0, idx) to entry.substring(idx + 1)
                } else {
                    entry to ""
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun writeAll(map: Map<String, String>) {
        val content = map.entries.joinToString("&") { "${it.key}=${it.value}" }
        val encrypted = encrypt(content)
        file.writeText(encrypted, charset)
    }

    private fun encrypt(text: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val bytes = cipher.doFinal(text.toByteArray(charset))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun decrypt(hex: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return String(cipher.doFinal(bytes), charset)
    }
}
