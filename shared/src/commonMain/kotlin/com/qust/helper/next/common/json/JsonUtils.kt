package com.qust.helper.next.common.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object JsonUtils {

	val json2obj = Json {
		encodeDefaults = true
		ignoreUnknownKeys = true
	}

	/**
	 * JSON 序列化，主要用于向外部传递的内容的序列化
	 *
	 * - 为 null 的 key 会被删除
	 * - 多态类不自动生成 type 字段
	 */
    @OptIn(ExperimentalSerializationApi::class)
    val obj2Json = Json {
		explicitNulls = false
		classDiscriminatorMode = ClassDiscriminatorMode.NONE

		encodeDefaults = true
		ignoreUnknownKeys = true
	}

}
