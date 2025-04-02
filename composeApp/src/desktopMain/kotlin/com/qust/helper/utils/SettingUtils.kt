package com.qust.helper.utils

import com.qust.helper.platform.utils.SettingUtilsImpl

class SettingImpl : SettingUtilsImpl() {

	override fun getString(key: String, defValue: String) = defValue
	override fun getInt(key: String, defValue: Int) = defValue
	override fun getBoolean(key: String, defValue: Boolean) = defValue
	override fun getFloat(key: String, defValue: Float) = defValue
	override fun getLong(key: String, defValue: Long) = defValue
	override fun getStringSet(key: String, defValue: Set<String>): Set<String>? = defValue

	override fun putString(key: String, value: String){ }
	override fun putInt(key: String, value: Int) { }
	override fun putBoolean(key: String, value: Boolean) { }
	override fun putFloat(key: String, value: Float) { }
	override fun putLong(key: String, value: Long) { }
	override fun putStringSet(key: String, value: Set<String>) { }

	override fun removeKey(key: String){ }
}

actual fun getSettingUtils(): SettingUtilsImpl = SettingImpl()