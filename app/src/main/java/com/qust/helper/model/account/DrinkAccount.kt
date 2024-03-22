package com.qust.helper.model.account

import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class DrinkAccount: Account(
	host = "dcxy-customer-app.dcrym.com",
	accountName = Keys.DRINK_ACCOUNT,
	passwordName = Keys.DRINK_PASSWORD,
	cookieName = "drinkCookie",
	scheme = "https"
) {

	private var userToken = Setting.getString("drinkToken")
		set(value){
			field = value
			Setting.edit { it.putString("drinkToken", value) }
		}

	var drinkCode = Setting.getString("drinkCode")
		set(value) {
			field = value
			Setting.edit { it.putString("drinkCode", value) }
		}

	companion object {
		fun getInstance(): DrinkAccount {
			return Instance.INSTANCE
		}
	}

	private object Instance {
		val INSTANCE = DrinkAccount()
	}

	override suspend fun absCheckLogin(): Boolean {
		if(userToken.isEmpty()) throw NeedLoginException()
		val js: JSONObject
		try {
			postNoCheck("app/customer/login",
				"{\"loginTime\": \"${System.currentTimeMillis()}\"}".toRequestBody("application/json".toMediaType()),
				Headers.headersOf("clientsource", "{}", "token", userToken)
			).use {
				js = JSONObject(it.body!!.string())
			}
		} catch(_: Exception) {
			return false
		}
		return if(js.getInt("code") != 1000) {
			userToken = ""
			false
		}else{
			true
		}
	}

	override suspend fun absLogin(account: String, password: String): Boolean {
		val js: JSONObject

		try {
			postNoCheck(
				"app/customer/login",
				JSONObject().apply {
					put("loginAccount", account)
					put("password", password)
				}.toString().toRequestBody("application/json".toMediaType()),
				Headers.headersOf("clientsource", "{}")
			).use {
				js = JSONObject(it.body!!.string())
			}
		}catch(e: Exception){
			throw IOException(e)
		}


		return if(js.getInt("code") == 1000) {
			userToken = js.getJSONObject("data").getString("token")
			true
		} else {
			throw IOException(js.getString("msg"))
		}
	}

	@Throws(IOException::class)
	suspend fun getDrinkCode(){
		val js: JSONObject
		try {
			getNoCheck("app/customer/flush/idbar", Headers.headersOf("clientsource", "{}", "token", userToken)).use {
				js = JSONObject(it.body!!.string())
			}
		}catch(e: Exception){
			throw IOException(e)
		}

		val data: String = js.getString("data")
		drinkCode = data.substring(0, data.length - 1) + "3"
	}
}