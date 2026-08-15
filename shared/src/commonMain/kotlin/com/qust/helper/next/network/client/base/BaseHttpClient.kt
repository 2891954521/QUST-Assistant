package com.qust.helper.next.network.client.base

import com.qust.helper.next.AppConfig
import com.qust.helper.next.common.Platform
import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.json.JsonUtils
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.common.network.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class BaseHttpClient {

	val client by lazy { createHttpClient(::buildClient) }

	/**
	 * 子类扩展 HttpClient 功能的函数
	 */
	open fun buildClient(config: HttpClientConfig<*>) {

	}

	/**
	 * 请求发出前调用的函数，可以在此处对请求进行修改，如：添加 Authorization header
	 */
	open fun buildRequest(builder: HttpRequestBuilder): HttpRequestBuilder {
		return builder
	}

	suspend inline fun <reified T> executeAs(builder: HttpRequestBuilder): T = executeAs(builder, typeOf<T>())

	suspend inline fun <reified T> executeRaw(builder: HttpRequestBuilder): T = executeRaw(builder, typeInfo<T>())

	/**
	 * 执行请求，响应为Json
	 */
	suspend fun <T> executeAs(builder: HttpRequestBuilder, type: KType): T {
		var body: String? = null

		try {
			val b: String = execute(builder, typeInfo<String>())
			body = b
			val data = JsonUtils.json2obj.decodeFromString(serializer(type), b)
			return data as T
		} catch(e: UserDisplayException) {
			throw e

		} catch(e: ClassCastException) {
			throw UserDisplayException("请求响应解析错误", RuntimeException("http call error, url: ${builder.url}, response: ${body ?: ""}", e))

		} catch(e: Exception) {
			throw UserDisplayException("请求失败", RuntimeException("http call error, url: ${builder.url}, response: ${body ?: ""}", e))
		}
	}

	/**
	 * 执行请求，响应为字符串
	 */
	suspend fun executeAsString(builder: HttpRequestBuilder): String {
		try {
			return execute(builder, typeInfo<String>())
		} catch(e: UserDisplayException) {
			throw e
		} catch(e: Exception) {
			throw UserDisplayException("请求失败", RuntimeException("http call error, url: ${builder.url}", e))
		}
	}

	/**
	 * 执行请求，响应类型自定义
	 *
	 * @param type 响应类型
	 */
	suspend fun <T> executeRaw(builder: HttpRequestBuilder, type: TypeInfo): T {
		try {
			return execute(builder, type)
		} catch(e: UserDisplayException) {
			throw e
		} catch(e: Exception) {
			throw UserDisplayException("请求失败", RuntimeException("http call error, url: ${builder.url}", e))
		}
	}

	/**
	 * 执行请求
	 *
	 * @param type 响应类型
	 */
	protected open suspend fun <T> execute(builder: HttpRequestBuilder, type: TypeInfo): T {
		return client.request(buildRequest(builder)).body(type)
	}


	protected fun createHttpClient(block: HttpClientConfig<*>.() -> Unit) = HttpClient {
		install(ContentNegotiation) {
			json()
		}

		engine {
			if(AppConfig.HTTP_PROXY.isNotEmpty() && Platform.currentPlatform() != Platform.PlatformType.WEB) {
				Logger.i("use proxy: ${AppConfig.HTTP_PROXY}")
				proxy = ProxyBuilder.http(AppConfig.HTTP_PROXY)
			}
		}

		block(this)
	}
}