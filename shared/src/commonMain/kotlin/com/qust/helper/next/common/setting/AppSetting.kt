package com.qust.helper.next.common.setting

import com.russhwolf.settings.Settings

object AppSetting: Settings by platformSettings() {

	operator fun contains(key: String): Boolean = hasKey(key)

	operator fun minusAssign(key: String): Unit = remove(key)

	operator fun get(key: String, defaultValue: Int): Int = getInt(key, defaultValue)

	operator fun get(key: String, defaultValue: Long): Long = getLong(key, defaultValue)

	operator fun get(key: String, defaultValue: String): String = getString(key, defaultValue)

	operator fun get(key: String, defaultValue: Float): Float = getFloat(key, defaultValue)

	operator fun get(key: String, defaultValue: Double): Double = getDouble(key, defaultValue)

	operator fun get(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)

	operator fun get(key: String, default: () -> Boolean): Boolean = getBooleanOrNull(key) ?: default()

	operator fun set(key: String, value: Int): Unit = putInt(key, value)

	operator fun set(key: String, value: Long): Unit = putLong(key, value)

	operator fun set(key: String, value: String): Unit = putString(key, value)

	operator fun set(key: String, value: Float): Unit = putFloat(key, value)

	operator fun set(key: String, value: Double): Unit = putDouble(key, value)

	operator fun set(key: String, value: Boolean): Unit = putBoolean(key, value)

	operator fun set(key: String, @Suppress("UNUSED_PARAMETER") value: Nothing?): Unit = remove(key)

	inline operator fun <reified T : Any> get(key: String): T? = when (T::class) {
		Int::class -> getIntOrNull(key) as T?
		Long::class -> getLongOrNull(key) as T?
		String::class -> getStringOrNull(key) as T?
		Float::class -> getFloatOrNull(key) as T?
		Double::class -> getDoubleOrNull(key) as T?
		Boolean::class -> getBooleanOrNull(key) as T?
		else -> throw IllegalArgumentException("Invalid type!")
	}

	inline operator fun <reified T : Any> set(key: String, value: T?): Unit =
		if (value == null) {
			this -= key
		} else when (T::class) {
			Int::class -> putInt(key, value as Int)
			Long::class -> putLong(key, value as Long)
			String::class -> putString(key, value as String)
			Float::class -> putFloat(key, value as Float)
			Double::class -> putDouble(key, value as Double)
			Boolean::class -> putBoolean(key, value as Boolean)
			else -> throw IllegalArgumentException("Invalid type!")
		}
}

expect fun platformSettings(): Settings
