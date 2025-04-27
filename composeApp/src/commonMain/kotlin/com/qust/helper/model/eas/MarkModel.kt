package com.qust.helper.model.eas

import com.qust.helper.data.QustApi
import com.qust.helper.entity.eas.Mark
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.database.MarkStorage
import com.qust.helper.model.database.getMarkStorage
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


object MarkModel: MarkStorage by getMarkStorage()  {

	/**
	 * 查询课表信息
	 * @param year 学年
	 * @param term 学期
	 */
	suspend fun queryMarks(easAccount: EasAccount, term: Int, xnm: String, xqm: String): List<Mark> {
		val markMap = HashMap<String, Mark.Builder>(32)

		try {
			val json = easAccount.post<String>(QustApi.GET_MARK){
				setBody(FormDataContent(parameters {
					append("xnm", xnm)
					append("xqm", xqm)
					append("queryModel.showCount", "999")
				}))
			}

			JsonUtils.parseString<JsonObject>(json)["items", JSONArray]?.forEach { item ->
				val js = item.jsonObject
				val name = js["kcmc", ""]
				if(!markMap.containsKey(name)) {
					markMap[name] = Mark.Builder(js)
				}
			}
		} catch(_: Exception) {
			return emptyList()
		}

		try {
			val json = easAccount.post<String>(QustApi.GET_MARK_DETAIL){
				setBody(FormDataContent(parameters {
					append("xnm", xnm)
					append("xqm", xqm)
					append("queryModel.showCount", "999")
				}))
			}

			JsonUtils.parseString<JsonObject>(json)["items", JSONArray]?.forEach { item ->
				val js = item.jsonObject
				val name = js["kcmc", ""]
				var mark = markMap[name]
				if(mark == null) {
					mark = Mark.Builder(js)
					markMap[mark.name] = mark
				}
				mark.addItemMark(js)
			}
		} catch(e: Exception) {
			e.printStackTrace()
		}
		return markMap.values.map { it.build(term, true) }
	}

}