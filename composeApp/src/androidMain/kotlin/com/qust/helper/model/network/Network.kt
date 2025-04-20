package com.qust.helper.model.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpClient(config: (HttpClientConfig<*>) -> Unit) = HttpClient(OkHttp, config)