package com.qust.helper.utils

import com.qust.helper.platform.utils.SettingUtilsImpl
import com.tencent.mmkv.MMKV

class MmkvSettingImpl : SettingUtilsImpl() {

	private val mmkv = MMKV.defaultMMKV()

	override fun getString(key: String, defValue: String) = mmkv.getString(key, defValue)
	override fun getInt(key: String, defValue: Int) = mmkv.getInt(key, defValue)
	override fun getBoolean(key: String, defValue: Boolean) = mmkv.getBoolean(key, defValue)
	override fun getFloat(key: String, defValue: Float) = mmkv.getFloat(key, defValue)
	override fun getLong(key: String, defValue: Long) = mmkv.getLong(key, defValue)
	override fun getStringSet(key: String, defValue: Set<String>): Set<String>? = mmkv.getStringSet(key, defValue)

	override fun putString(key: String, value: String){ mmkv.putString(key, value) }
	override fun putInt(key: String, value: Int) { mmkv.putInt(key, value) }
	override fun putBoolean(key: String, value: Boolean) { mmkv.putBoolean(key, value) }
	override fun putFloat(key: String, value: Float) { mmkv.putFloat(key, value) }
	override fun putLong(key: String, value: Long) { mmkv.putLong(key, value) }
	override fun putStringSet(key: String, value: Set<String>) { mmkv.putStringSet(key, value) }

	override fun removeKey(key: String) = mmkv.removeValueForKey(key)
}

actual fun getSettingUtils(): SettingUtilsImpl = MmkvSettingImpl()