package com.qust.helper.next.module.eas

import com.qust.helper.next.common.json.get
import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.network.client.EasHttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object MarkModel {

	suspend fun queryMarks(term: Int, xnm: String, xqm: String): List<Mark> {
		val markMap = HashMap<String, Mark.Builder>(32)

		try {
			val json = EasHttpClient.post<FormDataContent, JsonObject>(
				url = "jwglxt/cjcx/cjcx_cxXsgrcj.html",
				params = mapOf("doType" to "query"),
				body = FormDataContent(parameters {
					append("xnm", xnm)
					append("xqm", xqm)
					append("queryModel.showCount", "999")
				})
			)

			json["items"]?.jsonArray?.forEach { item ->
				val js = item.jsonObject
				val name = js["kcmc", ""].trim()
				if(name.isEmpty()) return@forEach
				if(!markMap.containsKey(name)) {
					markMap[name] = Mark.Builder(js)
				}
			}
		} catch(_: Exception) {
			return emptyList()
		}

		try {
			val json = EasHttpClient.post<FormDataContent, JsonObject>(
				url = "jwglxt/cjcx/cjcx_cxXsKccjList.html",
				body = FormDataContent(parameters {
					append("xnm", xnm)
					append("xqm", xqm)
					append("queryModel.showCount", "999")
				})
			)

			json["items"]?.jsonArray?.forEach { item ->
				val js = item.jsonObject
				val name = js["kcmc", ""].trim()
				if(name.isEmpty()) return@forEach
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

		return markMap.values.map { it.build(term, new = true) }
	}
}
