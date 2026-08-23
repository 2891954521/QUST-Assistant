package com.qust.helper.utils

/**
 * 跨平台安全存储接口，用于存储账号密码等敏感信息。
 *
 * Android: 使用 MMKV 加密存储
 * Desktop: 使用 JCE AES 加密文件存储
 */
expect object SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String, defValue: String): String
    fun putStringSet(key: String, value: Set<String>)
    fun getStringSet(key: String, defValue: Set<String>): Set<String>
    fun remove(key: String)
    fun contains(key: String): Boolean
}
