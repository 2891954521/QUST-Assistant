package com.qust.helper.next.ui.router.params

import kotlin.reflect.KClass
import kotlin.reflect.KType

object EmptyPageParam: PageParam {
    override fun getInt(key: String): Int? = null
    override fun getLong(key: String): Long? = null
    override fun getFloat(key: String): Float? = null
    override fun getDouble(key: String): Double? = null
    override fun getString(key: String): String? = null
    override fun getBoolean(key: String): Boolean? = null
    override fun <T : Any> getSerializable(key: String, clazz: KClass<T>, type: KType): T? = null
}