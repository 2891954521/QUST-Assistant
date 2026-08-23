package com.qust.helper.utils

import com.qust.helper.App
import com.tencent.mmkv.MMKV

actual class SecureStorage {

    private val secureMmkv: MMKV by lazy {
        val key = "qust_helper_secure_key_2024_v1.0.0"
        MMKV.defaultMMKV().encryptWithKey(key)
    }

    actual fun putString(key: String, value: String) {
        secureMmkv.putString(key, value)
    }

    actual fun getString(key: String, defValue: String): String {
        return secureMmkv.getString(key, defValue) ?: defValue
    }

    actual fun remove(key: String) {
        secureMmkv.removeValueForKey(key)
    }

    actual fun contains(key: String): Boolean {
        return secureMmkv.containsKey(key)
    }
}
