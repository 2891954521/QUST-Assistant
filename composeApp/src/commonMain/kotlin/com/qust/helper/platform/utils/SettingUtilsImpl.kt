package com.qust.helper.platform.utils

abstract class SettingUtilsImpl {

	abstract fun getString(key: String, defValue: String): String?
	abstract fun getInt(key: String, defValue: Int): Int
	abstract fun getBoolean(key: String, defValue: Boolean): Boolean
	abstract fun getFloat(key: String, defValue: Float): Float
	abstract fun getStringSet(key: String, defValue: Set<String>): Set<String>?

	abstract fun putString(key: String, value: String)
	abstract fun putInt(key: String, value: Int)
	abstract fun putBoolean(key: String, value: Boolean)
	abstract fun putFloat(key: String, value: Float)
	abstract fun putStringSet(key: String, value: Set<String>)

	abstract fun removeKey(key: String)
}