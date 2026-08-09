package com.qust.helper.next.ui.router.params

import com.qust.helper.next.common.json.deepCopy
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class MapPageParamBuilder: PageParamBuilder {

    val map = hashMapOf<String, Any>()

    override fun putBoolean(key: String, value: Boolean) {
        map[key] = value
    }

    override fun putInt(key: String, value: Int) {
        map[key] = value
    }

    override fun putLong(key: String, value: Long) {
        map[key] = value
    }

    override fun putFloat(key: String, value: Float) {
        map[key] = value
    }

    override fun putDouble(key: String, value: Double) {
        map[key] = value
    }

    override fun putString(key: String, value: String) {
        map[key] = value
    }

    override fun <T: Any> putSerializable(key: String, value: T, type: KType) {
        map[key] = value.deepCopy(serializer(type)) as Any
    }

    override fun build(): MapPageParam = MapPageParam(map)
}
