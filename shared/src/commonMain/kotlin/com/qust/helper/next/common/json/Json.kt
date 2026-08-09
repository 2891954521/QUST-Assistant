package com.qust.helper.next.common.json

import com.qust.helper.next.common.json.JsonUtils.json2obj
import com.qust.helper.next.common.json.JsonUtils.obj2Json
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer


/**
 * 基于序列化反/序列化的深拷贝
 */
inline fun <reified T> T.deepCopy(): T = deepCopy(serializer())

/**
 * 基于序列化反/序列化的深拷贝
 */
fun <T> T.deepCopy(serializer: KSerializer<T>): T {
    val jsonString = obj2Json.encodeToString(serializer, this)
    return obj2Json.decodeFromString(serializer, jsonString)
}


/**
 * 将任意数据转换为 [JsonElement] 表示。转换支持原始数据类型、[Map]、[List] 和 null
 *
 * 对于不支持的数据类型，由于无法得到对应类的序列化器，因此会抛出异常
 */
@OptIn(InternalSerializationApi::class)
fun Any?.parseToJson(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> JsonArray(map { it.parseToJson() })
    is List<*> -> JsonArray(map { it.parseToJson() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.parseToJson() }.toMap())
    else -> throw IllegalArgumentException("不支持序列化类型：${this::class.simpleName}")
}

/**
 * 将 [JsonElement] 转换为对应的 Kotlin 对象。
 *
 * - [JsonObject] 转为 [Map]
 * - [JsonArray] 转为 [List]
 * - [JsonPrimitive] 根据值的具体类型（[Int]、[Boolean]、[String]、[Long]、[Double]）转为对应类型
 * - 其他 -> null
 */
fun JsonElement.parseToObject(): Any? = when (this) {
    is JsonObject -> this.mapValues { it.value.parseToObject() }

    is JsonArray -> this.map { it.parseToObject() }

    is JsonPrimitive -> when {
        isString -> content
        booleanOrNull != null -> boolean
        intOrNull != null -> int
        longOrNull != null -> long
        doubleOrNull != null -> double
        else -> null
    }
}


// ==============================
// 对象编码为字符串
// ==============================

inline fun <reified T> T.encodeToJson(): String = obj2Json.encodeToString<T>(this)

inline fun <reified T> Collection<T>.encodeToJson(): String = obj2Json.encodeToString(this)

fun JsonElement.encodeToJson(): String = obj2Json.encodeToString(this)


// ==============================
// 字符串解码为对象
// ==============================

inline fun <reified T> String.decodeToObject() = json2obj.decodeFromString<T>(this)

inline fun <reified T> String.decodeToList() = json2obj.decodeFromString<List<T>>(this)

fun String.decodeToJson(): JsonElement = json2obj.parseToJsonElement(this)

fun String.decodeToJsonObject(): JsonObject = json2obj.parseToJsonElement(this) as JsonObject

fun String.decodeToJsonArray(): JsonArray = json2obj.parseToJsonElement(this) as JsonArray


// ==============================
//  Json Kotlin对象操作
// ==============================

operator fun JsonObject.get(key: String, def: Int) = (this[key] as? JsonPrimitive)?.intOrNull ?: def
operator fun JsonObject.get(key: String, def: Long) = (this[key] as? JsonPrimitive)?.longOrNull ?: def
operator fun JsonObject.get(key: String, def: Float) = (this[key] as? JsonPrimitive)?.floatOrNull ?: def
operator fun JsonObject.get(key: String, def: String) = (this[key] as? JsonPrimitive)?.contentOrNull ?: def
operator fun JsonObject.get(key: String, def: Double) = (this[key] as? JsonPrimitive)?.doubleOrNull ?: def
operator fun JsonObject.get(key: String, def: Boolean) = (this[key] as? JsonPrimitive)?.booleanOrNull ?: def

/**
 * 将 [JsonObject] 转换为 [Map]
 */
fun JsonObject.toMap(): Map<String, Any?> = this.mapValues { it.value.parseToObject() }

/**
 * 将 [JsonArray] 转换为 [List]
 */
fun JsonArray.toList(): List<Any?> = this.map { it.parseToObject() }

/**
 * 从可变数量的键值对构建 JSON 表示
 *
 * @param pairs JSON中需要包含的键值对。每对由字符串键和任意类型的可选值组成。
 *
 * @return 一个代表最终 JSON 结构的 JsonElement
 */
fun jsonOf(vararg pairs: Pair<String, Any?> = emptyArray()): JsonElement = mapOf(*pairs).parseToJson()

