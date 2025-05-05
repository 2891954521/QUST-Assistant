package com.qust.helper.model.eas

import com.qust.helper.data.QustApi
import com.qust.helper.entity.eas.Notice
import com.qust.helper.model.account.EasAccount
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object NoticeModel {

	/**
	 * 查询教务通知
	 * @param page         第几页
	 * @param pageSize     每页数量
	 */
	suspend fun queryNotice(account: EasAccount, page: Int = 1, pageSize: Int = 1): List<Notice> {
		try{
			val body = account.post<String>(QustApi.EA_SYSTEM_NOTICE){
				setBody(FormDataContent(parameters {
					append("queryModel.showCount", pageSize.toString())
					append("queryModel.currentPage", page.toString())
					append("queryModel.sortName", "cjsj")
					append("queryModel.sortOrder", "desc")
				}))
			}
			if(body.startsWith("{") || body.startsWith("[")) {
				val item = JsonUtils.parseString<JsonObject>(body)["items", JSONArray] ?: return emptyList()
				return List(item.size){ Notice.createFromJson(item[it].jsonObject) }
			}else{
				return emptyList()
			}
		}catch(e: Exception){
			Logger.e("'url:'${QustApi.EA_SYSTEM_NOTICE}", e)
			return emptyList()
		}
	}

}