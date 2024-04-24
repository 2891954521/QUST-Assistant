package com.qust.helper.model.account

import com.qust.helper.utils.VpnEncodeUtils
import okhttp3.Cookie
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class VpnAccount(
	private val host: String,
	private val scheme: String = "https",
	private val ipass: IPassAccount = IPassAccount.getInstance()
): IAccount{

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
		return ipass.checkLogin()
	}

	override suspend fun login(): Boolean {
		return ipass.login()
	}

	override suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		return ipass.login(account, password, saveData)
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
}