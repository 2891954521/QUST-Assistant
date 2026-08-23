package com.qust.helper.model.account

import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Keys
import com.qust.helper.utils.JSONObject
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import com.qust.helper.utils.SettingUtils
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import kotlinx.serialization.json.JsonObject

object DrinkAccount: Account(
	protocol = URLProtocol.HTTPS,
	host = "dcxy-customer-app.dcrym.com",
	accountKey = Keys.DRINK_ACCOUNT,
	passwordKey = Keys.DRINK_PASSWORD,
	cookieKey = "drinkCookie"
) {

	private var userToken = SettingUtils.getString("drinkToken")
		set(value){
			field = value
			SettingUtils["drinkToken"] = value
		}

	val _drinkCode = mutableStateOf(SettingUtils.getString("drinkCode"))
	fun setDrinkCodeValue(value: String) {
		_drinkCode.value = value
		SettingUtils["drinkCode"] = value
	}

	override suspend fun baseLogin(account: String, password: String): Boolean {
		try {
			val resp = postOriginal("app/customer/login"){
				headers.append("Content-Type", "application/json;charset=UTF-8")
				setBody("""{"loginAccount": "$account", "password": "$password"}""")
			}.bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			if(js["code", 0] == 1000) {
				val data = js["data", JSONObject] ?: return false
				userToken = data["token", ""]
				return userToken.isNotEmpty()
			} else {
				return false
			}
		}catch(e: Exception){
			Logger.e(e = e)
			return false
		}
	}

	override suspend fun execute(request: HttpRequestBuilder): HttpResponse {
		request.headers.append("clientsource", "{}")
		request.headers.append("token", userToken)
		return super.execute(request)
	}

	suspend fun getDrinkCode(){
		val js = JsonUtils.parseString<JsonObject>(get<String>("app/customer/flush/idbar"))
		val data: String = js["data", ""]
		setDrinkCodeValue(data.substring(0, data.length - 1) + "3")
	}

}