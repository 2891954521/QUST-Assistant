package com.qust.helper.next.common.setting

import com.russhwolf.settings.Settings


class MemorySetting: Settings {

    private val map: MutableMap<String, Any> = mutableMapOf()

    override val keys: Set<String>
        get() = map.keys

    override val size: Int
        get() = map.size

    override fun clear() = map.clear()

    override fun remove(key: String){
        map.remove(key)
    }

    override fun hasKey(key: String): Boolean = map.containsKey(key)

    override fun putInt(key: String, value: Int){
        map[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int = map[key] as Int? ?: defaultValue

    override fun getIntOrNull(key: String): Int? = map[key] as Int?

    override fun putLong(key: String, value: Long){
        map[key] = value
    }

    override fun getLong(key: String, defaultValue: Long): Long = map[key] as Long? ?: defaultValue

    override fun getLongOrNull(key: String): Long? = map[key] as Long?

    override fun putString(key: String, value: String){
        map[key] = value
    }

    override fun getString(key: String, defaultValue: String): String = map[key] as String? ?: defaultValue

    override fun getStringOrNull(key: String): String? = map[key] as String?

    override fun putFloat(key: String, value: Float){
        map[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float = map[key] as Float? ?: defaultValue

    override fun getFloatOrNull(key: String): Float? = map[key] as Float?

    override fun putDouble(key: String, value: Double){
        map[key] = value
    }

    override fun getDouble(key: String, defaultValue: Double): Double = map[key] as Double? ?: defaultValue

    override fun getDoubleOrNull(key: String): Double? = map[key] as Double?

    override fun putBoolean(key: String, value: Boolean){
        map[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as Boolean? ?: defaultValue

    override fun getBooleanOrNull(key: String): Boolean? = map[key] as Boolean?

}