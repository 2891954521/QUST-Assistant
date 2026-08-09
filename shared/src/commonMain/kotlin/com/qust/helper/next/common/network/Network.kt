package com.qust.helper.next.common.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun HttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient