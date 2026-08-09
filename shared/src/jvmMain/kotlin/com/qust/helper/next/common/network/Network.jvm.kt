package com.qust.helper.next.common.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object JvmNetwork {
    // 信任所有证书的 TrustManager
    val trustAllCerts = @Suppress("CustomX509TrustManager") object : X509TrustManager {

        @Suppress("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) { }

        @Suppress("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) { }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    val sslSocketFactory: SSLSocketFactory = run {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
        sslContext.socketFactory
    }
}

actual fun HttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(OkHttp){
    engine {
        config {
            connectTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            sslSocketFactory(JvmNetwork.sslSocketFactory, JvmNetwork.trustAllCerts)
            hostnameVerifier { _, _ -> true }
        }
    }
    config()
}