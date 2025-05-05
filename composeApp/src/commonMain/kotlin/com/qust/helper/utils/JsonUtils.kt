package com.qust.helper.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long

object JSONObject
object JSONArray
object JSONParam

object JsonUtils {

	val json = Json { ignoreUnknownKeys = true }

	fun <T> parseString(string: String) = json.parseToJsonElement(string) as T

	operator fun JsonObject.get(key: String, def: Int) = (this[key] as? JsonPrimitive)?.int ?: def
	operator fun JsonObject.get(key: String, def: Long) = (this[key] as? JsonPrimitive)?.long ?: def
	operator fun JsonObject.get(key: String, def: Float) = (this[key] as? JsonPrimitive)?.float ?: def
	operator fun JsonObject.get(key: String, def: String) = (this[key] as? JsonPrimitive)?.content ?: def
	operator fun JsonObject.get(key: String, def: Double) = (this[key] as? JsonPrimitive)?.double ?: def
	operator fun JsonObject.get(key: String, def: Boolean) = (this[key] as? JsonPrimitive)?.boolean ?: def

	operator fun JsonObject.get(key: String, type: JSONObject) = (this[key] as? JsonObject)
	operator fun JsonObject.get(key: String, type: JSONArray) = (this[key] as? JsonArray)
	operator fun JsonObject.get(key: String, type: JSONParam) = (this[key] as? JsonPrimitive)
}
