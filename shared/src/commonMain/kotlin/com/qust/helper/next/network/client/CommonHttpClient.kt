package com.qust.helper.next.network.client

import com.qust.helper.next.network.client.base.BaseAppHttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.takeFrom

/**
 * 通用HTTP客户端
 */
object CommonHttpClient: BaseAppHttpClient() {

    /**
     * 不提供任何 baseUrl，所有 url 均应为完整url，不能为相对路径
     */
    override val baseUrl: String = ""

    override fun buildUrl(builder: HttpRequestBuilder, url: String, params: Map<String, Any?>?): HttpRequestBuilder {
        builder.url.takeFrom(baseUrl)
        params?.forEach { (key, value) -> builder.parameter(key, value) }
        return builder
    }
}