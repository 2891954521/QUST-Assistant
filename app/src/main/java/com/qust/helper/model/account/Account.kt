package com.qust.helper.model.account

import com.qust.helper.data.Setting
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


class NeedLoginException : RuntimeException("需要登录")

class WrongAccountException: RuntimeException("用户名或密码错误")

class WrongLogicException(msg: String) : RuntimeException("逻辑错误: $msg")

/**
 * 自动持久化处理Cookie
 * @param host 网站host
 * @param scheme 协议类型 http / https
 * @param name SharedPreferences存储的字段名
 */
class AccountCookieJar(
	host: String,
	scheme: String = "http",
	val name: String
) : CookieJar {

	/**
	 * 储存Cookie
	 */
	private val cookieStore: HashMap<String, Cookie>

	private var cookiesList = ArrayList<Cookie>()

	init {

		val url: HttpUrl = HttpUrl.Builder().host(host).scheme(scheme).build()
		for(cookieString in Setting.getStringSet(name, HashSet())) {
			val cookie = Cookie.parse(url, cookieString)
			if(cookie != null) cookiesList.add(cookie)
		}

		cookieStore = HashMap(cookiesList.size)
		for(newCookie in cookiesList) cookieStore[newCookie.name] = newCookie
	}

	/**
	 * 清空所有Cookie
	 */
	fun clearCookies() {
		cookieStore.clear()
		cookiesList.clear()
	}

	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		for(newCookie in cookies) cookieStore[newCookie.name] = newCookie
		cookiesList = ArrayList(cookieStore.values)
		val encodedCookies: HashSet<String> = HashSet()
		for(cookie in cookies) encodedCookies.add(cookie.toString())
		Setting.edit{ it.putStringSet(name, encodedCookies) }
	}

	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		return cookiesList
	}
}


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
}

/**
 * 自动处理账号信息的类
 */
abstract class Account(
	protected var host: String,
	var accountName: String,
	var passwordName: String,
	cookieName: String,
	protected val scheme: String = "http"
): IAccount {

	override var isLogin: Boolean = false

	protected val cookieJar: AccountCookieJar = AccountCookieJar(host, scheme, cookieName)

	/**
	 * HTTP请求对象
	 */
	private val client: OkHttpClient = OkHttpClient.Builder()
		.cookieJar(cookieJar)
		.build()

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
			Setting.getString(accountName),
			Setting.getString(passwordName),
			saveData = false
		)
	}

	@Throws(IOException::class)
	override suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		isLogin = if(account == null || password == null) false else absLogin(account, password)
		if(saveData && isLogin) Setting.edit { it.putString(accountName, account).putString(passwordName, password) }
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
		return if(isLogin || checkLogin()) {
			client.newCall(Request.Builder()
				.url("${scheme}://${host}/${url}".toHttpUrl())
				.build()
			).execute()
		}else{
			throw NeedLoginException()
		}
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
		return Setting.getString(accountName, "")
	}


	@Throws(IOException::class)
	protected abstract suspend fun absCheckLogin(): Boolean

	@Throws(IOException::class)
	protected abstract suspend fun absLogin(account: String, password: String): Boolean
}