package com.qust.helper.next.network.client

import com.qust.helper.next.common.json.encodeToJson
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.content.NullBody
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.JsonElement


@OptIn(InternalAPI::class)
fun HttpRequestBuilder.body(body: JsonElement?) {
    this.body = body?.encodeToJson() ?: NullBody
    bodyType = typeInfo<String>()
}