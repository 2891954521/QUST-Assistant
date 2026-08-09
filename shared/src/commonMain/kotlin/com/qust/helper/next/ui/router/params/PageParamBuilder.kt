package com.qust.helper.next.ui.router.params

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface PageParamBuilder {
    fun putBoolean(key: String, value: Boolean)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putFloat(key: String, value: Float)

    fun putDouble(key: String, value: Double)

    fun putString(key: String, value: String)

    fun <T : Any> putSerializable(key: String, value: T, type: KType)

    fun build(): PageParam
}

inline fun <reified T: Any> PageParamBuilder.putSerializable(key: String, value: T) = putSerializable(key = key, value = value, type = typeOf<T>())
