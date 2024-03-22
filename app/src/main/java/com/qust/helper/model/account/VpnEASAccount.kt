package com.qust.helper.model.account

import com.qust.helper.data.Setting
import com.qust.helper.data.api.QustApi
import com.qust.helper.data.Keys
import com.qust.helper.utils.VpnEncodeUtils
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class VpnEASAccount(
	private val vpnAccount: IPassAccount = IPassAccount.getInstance()
): EASAccount(), IAccount by vpnAccount{

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
		Setting.edit { it.putInt(Keys.EA_HOST, index) }
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
		return vpnAccount.get(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class, NeedLoginException::class)
	override suspend fun post(url: String, body: RequestBody): Response {
		return vpnAccount.post(VpnEncodeUtils.encryptUrl(scheme, host, url), body)
	}

	@Throws(IOException::class)
	override suspend fun getNoCheck(url: String): Response {
		return vpnAccount.getNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	override suspend fun postNoCheck(url: String, requestBody: RequestBody): Response {
		return vpnAccount.postNoCheck(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}

	@Throws(IOException::class)
	override suspend fun getNoRedirect(url: String): Response {
		return vpnAccount.getNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url))
	}

	@Throws(IOException::class)
	override suspend fun postNoRedirect(url: String, requestBody: RequestBody): Response {
		return vpnAccount.postNoRedirect(VpnEncodeUtils.encryptUrl(scheme, host, url), requestBody)
	}
}