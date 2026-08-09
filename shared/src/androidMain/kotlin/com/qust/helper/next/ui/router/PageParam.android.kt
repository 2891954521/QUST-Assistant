package com.qust.helper.next.ui.router

import android.os.Bundle
import com.qust.helper.next.common.json.JsonUtils
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

class IntentPageParam(
    val bundle: Bundle
): PageParam {
    override fun getInt(key: String): Int? = if(bundle.containsKey(key)) bundle.getInt(key) else null
    override fun getLong(key: String): Long? = if(bundle.containsKey(key)) bundle.getLong(key) else null
    override fun getFloat(key: String): Float? = if(bundle.containsKey(key)) bundle.getFloat(key) else null
    override fun getDouble(key: String): Double? = if(bundle.containsKey(key)) bundle.getDouble(key) else null
    override fun getString(key: String): String? = bundle.getString(key)
    override fun getBoolean(key: String): Boolean? = if(bundle.containsKey(key)) bundle.getBoolean(key) else null

    override fun getInt(key: String, default: Int): Int = bundle.getInt(key, default)
    override fun getLong(key: String, default: Long): Long = bundle.getLong(key, default)
    override fun getFloat(key: String, default: Float): Float = bundle.getFloat(key, default)
    override fun getDouble(key: String, default: Double): Double = bundle.getDouble(key, default)
    override fun getString(key: String, default: String): String = bundle.getString(key, default)
    override fun getBoolean(key: String, default: Boolean): Boolean = bundle.getBoolean(key, default)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getSerializable(key: String, clazz: KClass<T>, type: KType): T? {
        val serializable = bundle.getString(key) ?: return null
        return JsonUtils.obj2Json.decodeFromString(serializer(type), serializable) as? T
    }
}

class AndroidPageParamBuilder: PageParamBuilder {

    val bundle = Bundle()

    override fun putBoolean(key: String, value: Boolean) {
        bundle.putBoolean(key, value)
    }

    override fun putInt(key: String, value: Int) {
        bundle.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        bundle.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        bundle.putFloat(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        bundle.putDouble(key, value)
    }

    override fun putString(key: String, value: String) {
        bundle.putString(key, value)
    }

    override fun <T: Any> putSerializable(key: String, value: T, type: KType) {
        bundle.putString(key, JsonUtils.obj2Json.encodeToString(serializer = serializer(type), value = value))
    }

    override fun build(): IntentPageParam {
        return IntentPageParam(bundle)
    }
}