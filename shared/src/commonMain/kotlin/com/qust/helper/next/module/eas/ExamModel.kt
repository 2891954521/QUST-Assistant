package com.qust.helper.next.module.eas

import com.qust.helper.next.entity.eas.Exam
import com.qust.helper.next.network.client.EasHttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ExamModel {

	/**
	 * 查询考试
	 * @param xnm  学年代码 20xx
	 * @param xqm  学期代码 12 | 3
	 */
	suspend fun queryExam(term: Int, xnm: String, xqm: String): List<Exam> {
		val json = EasHttpClient.post<FormDataContent, JsonObject>(
			url = "jwglxt/kwgl/kscx_cxXsksxxIndex.html",
			params = mapOf("doType" to "query"),
			body = FormDataContent(parameters {
				append("xnm", xnm)
				append("xqm", xqm)
				append("queryModel.showCount", "999")
			})
		)

		val item = json["items"]?.jsonArray ?: return emptyList()

		return List(item.size) {
			val js = item[it].jsonObject
			Exam(
				term = term,
				name = js["kcmc"]?.jsonPrimitive?.content ?: "",
				place = js["cdmc"]?.jsonPrimitive?.content ?: "",
				time = js["kssj"]?.jsonPrimitive?.content ?: "",
			)
		}
	}

}