package com.qust.helper.next.ui.router.params

import kotlin.reflect.KClass
import kotlin.reflect.KType

class MapPageParam(
    val param: Map<String, Any>
): PageParam {
    override fun getInt(key: String): Int? = param[key] as? Int?
    override fun getLong(key: String): Long? = param[key] as? Long?
    override fun getFloat(key: String): Float? = param[key] as? Float?
    override fun getDouble(key: String): Double? = param[key] as? Double?
    override fun getString(key: String): String? = param[key] as? String?
    override fun getBoolean(key: String): Boolean? = param[key] as? Boolean?

    override fun getInt(key: String, default: Int): Int = getInt(key) ?: default
    override fun getLong(key: String, default: Long): Long = getLong(key) ?: default
    override fun getFloat(key: String, default: Float): Float = getFloat(key) ?: default
    override fun getDouble(key: String, default: Double): Double = getDouble(key) ?: default
    override fun getString(key: String, default: String): String = getString(key) ?: default
    override fun getBoolean(key: String, default: Boolean): Boolean = getBoolean(key) ?: default

    override fun <T : Any> getSerializable(key: String, clazz: KClass<T>, type: KType): T? = param[key] as? T?
}