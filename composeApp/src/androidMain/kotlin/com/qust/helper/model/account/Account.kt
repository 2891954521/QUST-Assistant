package com.qust.helper.model.account

import com.qust.helper.utils.SettingUtils
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection


class NeedLoginException : RuntimeException("需要登录")

class WrongAccountException: RuntimeException("用户名或密码错误")

class WrongLogicException(msg: String) : RuntimeException("逻辑错误: $msg")

/**
 * 请求结果
 */
class RequestResult(
	val code: ResultCode,
	val msg: String
)


enum class ResultCode(code: Int) {
	None(0),

	NeedLogin(1),

	NameOrPwdError(2),

	NetWorkError(3),

	LogicError(4),

	Done(200)
}



/**
 * 自动持久化处理Cookie
 * @param host 网站host
 * @param scheme 协议类型 http / https
 */
class AccountCookieJar(val scheme: String = "http", val host: String, val cookieName: String) : CookieJar {
	var cookiesList = emptyList<Cookie>()

	init {
		loadCookie(scheme, host)
	}

	fun loadCookie(scheme: String, host: String){
		val tmp = ArrayList<Cookie>()
		val url: HttpUrl = HttpUrl.Builder().host(host).scheme(scheme).build()
		for(cookieString in SettingUtils.getStringSet(cookieName, HashSet())) {
			val cookie = Cookie.parse(url, cookieString)
			if(cookie != null) tmp.add(cookie)
		}
		cookiesList = tmp
	}

	/**
	 * 清空所有Cookie
	 */
	fun clearCookies() {
		cookiesList = emptyList()
	}

	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		cookiesList += cookies
		SettingUtils.putStringSet(cookieName, cookies.map { cookie: Cookie -> cookie.toString() }.toHashSet())
	}

	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		return cookiesList
	}
}


interface IAccount {

	/**
	 * Cookie是否有效
	 */
	var isLogin: Boolean

	/**
	 * 登录，使用储存的用户名和密码
	 */
	@Throws(IOException::class, NeedLoginException::class)
	suspend fun login(): Boolean

	/**
	 * 登录
	 * @param account 账号，为null则使用储存的账号
	 * @param password 密码，为null则使用储存的密码
	 * @param saveData 是否保存数据
	 * @return 登录是否成功，失败的情况下为账号或密码错误
	 * @throws IOException 网络错误
	 */
	@Throws(IOException::class)
	suspend fun login(account: String?, password: String?, saveData: Boolean = true): Boolean

	/**
	 * 检查登陆状态，有账号信息时自动进行登录
	 */
	@Throws(IOException::class, NeedLoginException::class)
	suspend fun checkLogin(): Boolean

	/**
	 * GET请求
	 */
	@Throws(IOException::class, NeedLoginException::class)
	suspend fun get(url: String): Response

	/**
	 * POST请求
	 */
	@Throws(IOException::class, NeedLoginException::class)
	suspend fun post(url: String, body: RequestBody): Response

	/**
	 * GET请求，不检查登录状态
	 */
	@Throws(IOException::class)
	suspend fun getNoCheck(url: String): Response

	/**
	 * POST请求，不检查登录状态
	 */
	@Throws(IOException::class)
	suspend fun postNoCheck(url: String, requestBody: RequestBody): Response

	/**
	 * GET请求，不跟随重定向
	 */
	@Throws(IOException::class)
	suspend fun getNoRedirect(url: String): Response

	/**
	 * POST请求，不跟随重定向
	 */
	@Throws(IOException::class)
	suspend fun postNoRedirect(url: String, requestBody: RequestBody): Response

	/**
	 * 获取账号
	 */
	fun getAccount(): String?

	/**
	 * 获取Cookie
	 */
	fun getCookie(): List<Cookie>

	/**
	 * 退出登录
	 */
	fun logout()

}

/**
 * 自动处理账号信息的类
 */
abstract class Account(host: String, var accountName: String, var passwordName: String, val scheme: String = "http", val cookieName: String): IAccount {

	var host: String = host
		protected set(value){
			cookieJar.loadCookie(scheme, value)
			field = value
		}

	override var isLogin: Boolean = false

	protected val cookieJar: AccountCookieJar = AccountCookieJar(scheme, host, cookieName)

	/**
	 * HTTP请求对象
	 */
	private val client: OkHttpClient = OkHttpClient.Builder().cookieJar(cookieJar).build()

	/**
	 * HTTP请求对象，不跟随重定向
	 */
	private val clientNoRedirect: OkHttpClient = OkHttpClient.Builder()
		.cookieJar(cookieJar)
		.followRedirects(false)
		.build()


	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun login(): Boolean {
		return login(
			SettingUtils.getString(accountName),
			SettingUtils.getString(passwordName),
			saveData = false
		)
	}

	@Throws(IOException::class)
	override suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		isLogin = if(account == null || password == null) false else absLogin(account, password)
		if(saveData && isLogin) {
			SettingUtils.putString(accountName, account ?: "")
			SettingUtils.putString(passwordName, password ?: "")
		}
		return isLogin
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun checkLogin(): Boolean{
		isLogin = if(absCheckLogin()) true else login()
		if(!isLogin) throw NeedLoginException()
		return true
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun get(url: String): Response{
		if(!isLogin) throw NeedLoginException()

		val request = Request.Builder().url("${scheme}://${host}/${url}".toHttpUrl()).build()
		val result = client.newCall(request).execute()
		val code = result.code
		if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP){
			if(result.header("Location")?.contains("login")!!){
				isLogin = login()
				if(!isLogin) throw NeedLoginException()
				return client.newCall(request).execute()
			}
		}
		return result
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun post(url: String, body: RequestBody): Response{
		return if(isLogin || checkLogin()) {
			client.newCall(Request.Builder()
				.url("${scheme}://${host}/${url}".toHttpUrl())
				.post(body)
				.build()
			).execute()
		} else {
			throw NeedLoginException()
		}
	}

	@Throws(IOException::class)
	override suspend fun getNoCheck(url: String): Response {
		return client.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.build()
		).execute()
	}

	@Throws(IOException::class)
	suspend fun getNoCheck(url: String, requestHeaders: Headers): Response {
		return client.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.headers(requestHeaders)
			.build()
		).execute()
	}

	@Throws(IOException::class)
	override suspend fun postNoCheck(url: String, requestBody: RequestBody): Response {
		return client.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.post(requestBody)
			.build()
		).execute()
	}

	@Throws(IOException::class)
	suspend fun postNoCheck(url: String, requestBody: RequestBody, requestHeaders: Headers): Response {
		return client.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.headers(requestHeaders)
			.post(requestBody)
			.build()
		).execute()
	}

	@Throws(IOException::class)
	override suspend fun getNoRedirect(url: String): Response {
		return clientNoRedirect.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.build()
		).execute()
	}

	@Throws(IOException::class)
	override suspend fun postNoRedirect(url: String, requestBody: RequestBody): Response {
		return clientNoRedirect.newCall(Request.Builder()
			.url("${scheme}://${host}/${url}".toHttpUrl())
			.post(requestBody)
			.build()
		).execute()
	}


	/**
	 * 获取账号
	 */
	override fun getAccount(): String {
		return SettingUtils.getString(accountName, "")
	}

	override fun getCookie(): List<Cookie> {
		return cookieJar.cookiesList
	}

	override fun logout() {
		cookieJar.clearCookies()
		SettingUtils.putString(accountName, "")
		SettingUtils.putString(passwordName, "")
		isLogin = false
	}

	@Throws(IOException::class)
	abstract suspend fun absCheckLogin(): Boolean

	@Throws(IOException::class)
	protected abstract suspend fun absLogin(account: String, password: String): Boolean
}