package com.qust.helper.next.ui.router.params

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface PageParam {

    fun getInt(key: String): Int?
    fun getLong(key: String): Long?
    fun getFloat(key: String): Float?
    fun getDouble(key: String): Double?
    fun getString(key: String): String?
    fun getBoolean(key: String): Boolean?
    fun <T : Any> getSerializable(key: String, clazz: KClass<T>, type: KType): T?

    fun getInt(key: String, default: Int): Int = getInt(key) ?: default
    fun getLong(key: String, default: Long): Long = getLong(key) ?: default
    fun getFloat(key: String, default: Float): Float = getFloat(key) ?: default
    fun getDouble(key: String, default: Double): Double = getDouble(key) ?: default
    fun getString(key: String, default: String): String = getString(key) ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = getBoolean(key) ?: default
}

inline fun <reified T: Any> PageParam.getSerializable(key: String): T? = getSerializable(key, T::class, typeOf<T>())

inline fun <reified T: Any> PageParam.getSerializable(key: String, default: T): T = getSerializable(key, T::class, typeOf<T>()) ?: default
