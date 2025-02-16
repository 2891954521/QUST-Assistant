package com.qust.helper.model.account

import com.qust.helper.utils.VpnEncodeUtils
import okhttp3.Cookie
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

open class VpnAccount(
	private val host: String,
	private val scheme: String = "https",
	private val ipass: IPassAccount = IPassAccount.getInstance()
): IAccount{

	var needLogin = true

	override var isLogin: Boolean
		get() = ipass.isLogin
		set(_) {}

	override fun getAccount(): String {
		return ipass.getAccount()
	}

	override fun getCookie(): List<Cookie> {
		return ipass.getCookie()
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun checkLogin(): Boolean {
		ipass.getNoRedirect("").use {
			val code: Int = it.code
			if(code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP){
				if(!it.header("Location")?.contains("login")!!){
					if(needLogin) {
						return afterLogin()
					}else{
						return true
					}
				} else{
					return login()
				}
			}else if(needLogin){
				return afterLogin()
			} else {
				return true
			}
		}
	}

	open suspend fun afterLogin(): Boolean{
		return true
	}

	override suspend fun login(): Boolean {
		ipass.login()
		if(!isLogin){
			needLogin = true
			throw NeedLoginException()
		}else{
			needLogin = false
		}
		return afterLogin()
	}

	override suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		ipass.login(account, password, saveData)
		if(!isLogin){
			needLogin = true
			throw NeedLoginException()
		}else{
			needLogin = false
		}
		return afterLogin()
	}

	@Throws(IOException::class, NeedLoginException::class)
	 suspend fun get(scheme: String, host: String, url: String): Response {
		return ipass.get(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class, NeedLoginException::class)
	 suspend fun post(scheme: String, host: String, url: String, body: RequestBody): Response {
		return ipass.post(VpnEncodeUtils.encryptUrl(scheme, host, url), body)
	}

	@Throws(IOException::class)
	 suspend fun getNoCheck(scheme: String, host: String, url: String): Response {
		return ipass.getNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	 suspend fun postNoCheck(scheme: String, host: String, url: String, requestBody: RequestBody): Response {
		return ipass.postNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}

	@Throws(IOException::class)
	 suspend fun getNoRedirect(scheme: String, host: String, url: String): Response {
		return ipass.getNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	 suspend fun postNoRedirect(scheme: String, host: String, url: String, requestBody: RequestBody): Response {
		return ipass.postNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}


	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun get(url: String): Response {
		return ipass.get(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun post(url: String, body: RequestBody): Response {
		return ipass.post(VpnEncodeUtils.encryptUrl(scheme, host, url), body)
	}

	@Throws(IOException::class)
	override suspend fun getNoCheck(url: String): Response {
		return ipass.getNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	override suspend fun postNoCheck(url: String, requestBody: RequestBody): Response {
		return ipass.postNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}

	@Throws(IOException::class)
	override suspend fun getNoRedirect(url: String): Response {
		return ipass.getNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	override suspend fun postNoRedirect(url: String, requestBody: RequestBody): Response {
		return ipass.postNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}

	override fun logout() {
		ipass.logout()
	}
}