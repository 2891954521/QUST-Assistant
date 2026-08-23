package com.qust.helper.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.qust.helper.App

actual object SecureStorage {

    private val prefs by lazy {
        val context = App.instance
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "qust_helper_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue) ?: defValue
    }

    actual fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    actual fun getStringSet(key: String, defValue: Set<String>): Set<String> {
        return prefs.getStringSet(key, defValue) ?: defValue
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
}
