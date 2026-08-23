package com.qust.helper.utils

import com.tencent.mmkv.MMKV

actual object SecureStorage {

    private val secureMmkv: MMKV by lazy {
        val key = "qust_helper_secure_key_2024_v1.0.0"
        MMKV.mmkvWithID("qust_helper_secure", MMKV.MULTI_PROCESS_MODE, key)
    }

    actual fun putString(key: String, value: String) {
        secureMmkv.putString(key, value)
    }

    actual fun getString(key: String, defValue: String): String {
        return secureMmkv.getString(key, defValue) ?: defValue
    }

    actual fun putStringSet(key: String, value: Set<String>) {
        secureMmkv.putStringSet(key, value)
    }

    actual fun getStringSet(key: String, defValue: Set<String>): Set<String> {
        return secureMmkv.getStringSet(key, defValue) ?: defValue
    }

    actual fun remove(key: String) {
        secureMmkv.removeValueForKey(key)
    }

    actual fun contains(key: String): Boolean {
        return secureMmkv.containsKey(key)
    }
}
