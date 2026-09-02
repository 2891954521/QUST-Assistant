package com.qust.helper.next.repository.thrid

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.next.common.exception.NeedLoginException
import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.network.client.DrinkCodeClient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object DrinkCodeRepository {

	val drinkCode: State<String> field = mutableStateOf(AppSetting.getString(DataKeys.DRINK_CODE, ""))

	suspend fun login(account: String, password: String) {
		DrinkCodeClient.login(account, password)
		AppSetting[DataKeys.DRINK_ACCOUNT] = account
		AppSetting[DataKeys.DRINK_PASSWORD] = password
	}

	suspend fun getDrinkCode() {
		if(AppSetting[DataKeys.DRINK_TOKEN, ""].isEmpty()) throw NeedLoginException("请先登录")

		val js = DrinkCodeClient.get<JsonObject>("/app/customer/flush/idbar")
		val data = js["data"]
		if(data == null || data.jsonPrimitive.contentOrNull == null) {
			val msg = js["msg"]?.jsonPrimitive?.contentOrNull ?: "无法获取饮水码"
			if(msg.contains("登录") || msg.contains("token", ignoreCase = true)) {
				throw NeedLoginException("请先登录")
			}
			throw UserDisplayException(msg)
		}
		val content = data.jsonPrimitive.content
		val code = content.substring(0, content.length - 1) + "3"
		drinkCode.value = code
		AppSetting[DataKeys.DRINK_CODE] = code
	}
}