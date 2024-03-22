package com.qust.helper.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object Setting {

	private lateinit var defaultSharedPreferences: SharedPreferences

	fun init(context: Context) {
		defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
	}

	/**
	 * 保存数据到 SharedPreferences
	 * @param key 键
	 * @param value 值
	 */
	operator fun set(key: String, value: Any?) {
		val editor = defaultSharedPreferences.edit()
		when(value){
			null -> editor.putString(key, null)
			is Int -> editor.putInt(key, value)
			is Boolean -> editor.putBoolean(key, value)
			is Float -> editor.putFloat(key, value)
			is Long -> editor.putLong(key, value)
			else -> editor.putString(key, value.toString())
		}
		editor.apply()
	}

	/**
	 * 获取 SharedPreferences 保存的值
	 * @param key 键
	 * @return 值
	 */
	operator fun get(key: String): String? {
		return defaultSharedPreferences.getString(key, null)
	}

	fun getString(key: String, defaultVal: String = ""): String {
		return get(key) ?: defaultVal
	}

	fun getStringSet(key: String, defaultVal: Set<String> = emptySet()): Set<String> {
		return defaultSharedPreferences.getStringSet(key, defaultVal)!!
	}

	fun getInt(key: String, defaultVal: Int = 0): Int {
		return defaultSharedPreferences.getInt(key, defaultVal)
	}

	fun getBoolean(key: String, defaultVal: Boolean = false): Boolean {
		return defaultSharedPreferences.getBoolean(key, defaultVal)
	}

	fun edit(block: (SharedPreferences.Editor) -> Unit){
		val editor = defaultSharedPreferences.edit()
		block(editor)
		editor.apply()
	}
}
