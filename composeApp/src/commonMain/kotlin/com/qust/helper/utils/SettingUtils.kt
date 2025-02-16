package com.qust.helper.utils

import com.qust.helper.platform.utils.SettingUtilsImpl

expect fun getSettingUtils(): SettingUtilsImpl

object SettingUtils {

	val impl = getSettingUtils()

	operator fun <T> get(key: String, defValue: T): T {
		return when(defValue){
			is String  -> (impl.getString(key, defValue) ?: defValue) as T
			is Int     -> impl.getInt(key, defValue) as T
			is Boolean -> impl.getBoolean(key, defValue) as T
			is Float   -> impl.getFloat(key, defValue) as T
			null -> throw IllegalArgumentException("unsupported mmkv value null")
			else -> throw IllegalArgumentException("unsupported mmkv value type ${defValue!!::class.simpleName}")
		}
	}

	operator fun <T> set(key: String, value: T){
		when(value){
			is String  -> impl.putString(key, value)
			is Int     -> impl.putInt(key, value)
			is Boolean -> impl.putBoolean(key, value)
			is Float   -> impl.putFloat(key, value)
			null -> throw IllegalArgumentException("unsupported mmkv value null")
			else -> throw IllegalArgumentException("unsupported mmkv value type ${value!!::class.simpleName}")
		}
	}

	fun getString(key: String, defValue: String = "") = impl.getString(key, defValue) ?: defValue
	fun putString(key: String, value: String) = impl.putString(key, value)

	fun getInt(key: String, defValue: Int = 0): Int = impl.getInt(key, defValue)
	fun putInt(key: String, value: Int) = impl.putInt(key, value)

	fun getBoolean(key: String, defValue: Boolean = false) = impl.getBoolean(key, defValue)
	fun putBoolean(key: String, value: Boolean) = impl.putBoolean(key, value)

	fun getStringSet(key: String, defValue: Set<String> = emptySet()) = impl.getStringSet(key, defValue) ?: emptySet()
	fun putStringSet(key: String, value: Set<String>) = impl.putStringSet(key, value)

	fun removeKey(key: String) = impl.removeKey(key)
}