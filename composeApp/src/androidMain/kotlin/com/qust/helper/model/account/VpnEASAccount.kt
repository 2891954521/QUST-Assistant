package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.QustApi
import com.qust.helper.utils.SettingUtils
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class VpnEASAccount: EASAccount(){

	private class VpnEas(host: String, scheme: String) : VpnAccount(host, scheme) {
		override suspend fun afterLogin(): Boolean {
			return getNoCheck("sso/driotlogin").code == 200
		}
	}

	private var vpnAccount: VpnAccount = VpnEas(host, scheme)

	companion object {
		fun getInstance(): VpnEASAccount {
			return Instance.INSTANCE
		}
	}

	private object Instance{
		val INSTANCE = VpnEASAccount()
	}

	override var isLogin: Boolean
		get() = vpnAccount.isLogin
		set(_) {}

	override fun changeHost(index: Int) {
		if(index >= QustApi.EA_HOSTS.size) return
		host = QustApi.EA_HOSTS[index]
		vpnAccount = VpnEas(host, scheme)
		SettingUtils.putInt(Keys.EA_HOST, index)
	}

	override fun getAccount(): String {
		return vpnAccount.getAccount()
	}

	override suspend fun checkLogin(): Boolean {
		return vpnAccount.checkLogin()
	}

	override suspend fun login(): Boolean {
		return vpnAccount.login()
	}

	override suspend fun login(account: String?, password: String?, saveData: Boolean): Boolean {
		return vpnAccount.login(account, password, saveData)
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun get(url: String): Response {
		return vpnAccount.get(url)
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun post(url: String, body: RequestBody): Response {
		return vpnAccount.post(url, body)
	}

	@Throws(IOException::class)
	override suspend fun getNoCheck(url: String): Response {
		return vpnAccount.getNoCheck(url)
	}

	@Throws(IOException::class)
	override suspend fun postNoCheck(url: String, requestBody: RequestBody): Response {
		return vpnAccount.postNoCheck(url, requestBody)
	}

	@Throws(IOException::class)
	override suspend fun getNoRedirect(url: String): Response {
		return vpnAccount.getNoRedirect(url)
	}

	@Throws(IOException::class)
	override suspend fun postNoRedirect(url: String, requestBody: RequestBody): Response {
		return vpnAccount.postNoRedirect(url, requestBody)
	}
}