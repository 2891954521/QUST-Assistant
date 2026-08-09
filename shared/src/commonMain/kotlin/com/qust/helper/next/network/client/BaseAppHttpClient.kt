package com.qust.helper.next.network.client

import com.qust.helper.next.common.json.JsonUtils
import com.qust.helper.next.common.json.parseToJson
import com.qust.helper.next.network.client.BaseHttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.content.NullBody
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 封装了基础 GET 和 POST 请求的Client
 *
 * 若需要自定义请求内容请使用父类的 executeAs 和 executeRaw
 */
abstract class BaseAppHttpClient: BaseHttpClient() {

    abstract val baseUrl: String

    val baseHttpUrl: URLBuilder by lazy { URLBuilder().takeFrom(baseUrl) }

    /**
     * 执行GET请求
     * @param params url参数
     */
    suspend inline fun <reified T> get(url: String, params: Map<String, Any?>? = null): T = get(url, params, typeOf<T>())

    /**
     * 执行GET请求
     * @param params url参数
     */
    suspend fun <T> get(url: String, params: Map<String, Any?>? = null, type: KType): T {
        val builder = HttpRequestBuilder()
        builder.method = HttpMethod.Get
        buildUrl(builder, url, params)
        return executeAs(builder, type)
    }

    /**
     * 执行POST请求
     * @param body 请求体，可选类型：
     * - data class / Map / [JsonElement] 对应 application/json
     * - [io.ktor.client.request.forms.FormDataContent] 对应 application/x-www-form-urlencoded
     * ```
     * FormDataContent(parameters {
     *     append("username", "JetBrains")
     *     append("email", "example@jetbrains.com")
     * })
     * ```
     * - [io.ktor.client.request.forms.MultiPartFormDataContent] 对应 multipart/form-data
     * ```
     * MultiPartFormDataContent(formData {
     *     append("name", "JetBrains logo")
     *     append("image1", File("logo.png").readBytes(), Headers.build {
     *         append(HttpHeaders.ContentType, "image/png")
     *         append(HttpHeaders.ContentDisposition, "filename=\"logo1.png\"")
     *     })
     *     append("image2", InputProvider { inputStream.asInput().buffered() }, Headers.build {
     *         append(HttpHeaders.ContentType, "image/png")
     *         append(HttpHeaders.ContentDisposition, "filename=\"logo2.png\"")
     *     })
     * })
     * ```
     */
    suspend inline fun <reified T, reified R> post(url: String, body: T? = null): R = post(url, body, typeInfo<T>(), typeOf<R>())

    /**
     * 执行POST请求
     * @param body 请求体
     */
    suspend fun <T, R> post(url: String, body: T? = null, bodyType: TypeInfo, respType: KType): R {
        val builder = HttpRequestBuilder()
        builder.method = HttpMethod.Post
        buildUrl(builder, url, null)
        buildBody(builder, body, bodyType)
        return executeAs(builder, respType)
    }

    protected open fun buildUrl(builder: HttpRequestBuilder, url: String, params: Map<String, Any?>? = null): HttpRequestBuilder {
        builder.url.takeFrom(baseHttpUrl)
        builder.url.appendPathSegments(url)
        params?.forEach { (key, value) -> builder.parameter(key, value) }
        return builder
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalAPI::class)
    private fun <T: Any> buildBody(builder: HttpRequestBuilder, body: T? = null, bodyType: TypeInfo): HttpRequestBuilder {
        if(body == null){
            builder.body = NullBody
            return builder
        }

        if(body::class.isData){
            builder.contentType(ContentType.Application.Json)
            builder.setBody(body, bodyType)
            return builder
        }

        when(body){
            is Map<*, *> -> {
                builder.contentType(ContentType.Application.Json)
                builder.body = JsonUtils.obj2Json.encodeToString(JsonElement.serializer(), body.parseToJson())
                builder.bodyType = null
                return builder
            }

            is JsonElement -> {
                builder.contentType(ContentType.Application.Json)
                builder.body = JsonUtils.obj2Json.encodeToString(JsonElement.serializer(), body)
                builder.bodyType = null
            }

            is FormDataContent -> {
                builder.contentType(ContentType.Application.FormUrlEncoded)
                builder.body = body
                builder.bodyType = null
            }

            is MultiPartFormDataContent -> {
                builder.contentType(ContentType.MultiPart.FormData)
                builder.body = body
                builder.bodyType = null
            }

            else -> {
                throw RuntimeException("请求体类型错误")
            }
        }

        return builder
    }

}