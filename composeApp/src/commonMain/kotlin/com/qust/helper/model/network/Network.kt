package com.qust.helper.model.network

import com.qust.helper.utils.SecureStorage
import com.qust.helper.utils.SettingUtils
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient

/**
 * Cookie 持久化类（加密存储）
 */
class AppCookiesStorage(
    private val cookieName: String
): CookiesStorage {

    private val mutex = Mutex()
    private val cookies = mutableListOf<Cookie>()

    init {
        SecureStorage.getStringSet(cookieName, emptySet()).forEach { cookieData ->
            val sp = cookieData.indexOfFirst { it == '=' }
            if(sp != -1 && sp < cookieData.length - 1){
                cookies.add(Cookie(cookieData.substring(0, sp), cookieData.substring(sp + 1)))
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        return cookies
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (cookie.name.isBlank() || cookie.maxAge == 0) return

        mutex.withLock {
            cookies.removeAll { it.name == cookie.name }
            cookies.add(Cookie(cookie.name, cookie.value))
            SecureStorage.putStringSet(cookieName, cookies.map { "${it.name}=${it.value}" }.toSet())
        }
    }

    suspend fun clear() = mutex.withLock {
        cookies.clear()
        SecureStorage.remove(cookieName)
    }

    override fun close() { }
}
