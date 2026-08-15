package com.qust.helper.next.network.client.token

import com.qust.helper.next.common.setting.AppSetting
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cookie 持久化类
 */
class AppCookiesStorage(
	private val cookieName: String
): CookiesStorage {

	private val mutex = Mutex()
	private val cookies = mutableListOf<Cookie>()

	init {
		AppSetting.getString(cookieName, "").split(";").forEach { cookieData ->
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
			AppSetting.putString(cookieName, cookies.map { "${it.name}=${it.value}" }.toSet().joinToString(";"))
		}
	}

	suspend fun clear() = mutex.withLock {
		cookies.clear()
	}

	override fun close() { }
}