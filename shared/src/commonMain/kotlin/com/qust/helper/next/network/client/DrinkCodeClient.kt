package com.qust.helper.next.network.client

import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.json.decodeToJsonObject
import com.qust.helper.next.common.json.get
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.network.client.base.BaseAppHttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.jsonObject

object DrinkCodeClient: BaseAppHttpClient() {

	override var baseUrl = "https://dcxy-customer-app.dcrym.com/"

	private var token = AppSetting.getString(DataKeys.DRINK_TOKEN, "")
		set(value){
			field = value
			AppSetting[DataKeys.DRINK_TOKEN] = value
		}

	override fun buildRequest(builder: HttpRequestBuilder): HttpRequestBuilder {
		builder.headers.append("clientsource", "{}")
		builder.headers.append("token", token)
		return builder
	}

	suspend fun login(account: String, password: String): Boolean {
		val request = buildPostRequest(
			url = "app/customer/login",
			body = mapOf("loginAccount" to account, "password" to password)
		)

		val js = executeAsString(request).decodeToJsonObject()

		if(js["code", 0] != 1000) throw UserDisplayException(js["msg", ""].takeIf(String::isNotEmpty) ?: "登录失败")

		val data = js["data"]?.jsonObject ?: throw UserDisplayException(js["msg", ""].takeIf(String::isNotEmpty) ?: "登录失败")
		val token = data["token", ""]

		if(token.isNotEmpty()){
			this.token = token
			return true
		}else{
			throw UserDisplayException(js["msg", ""].takeIf(String::isNotEmpty) ?: "登录失败")
		}
	}
}