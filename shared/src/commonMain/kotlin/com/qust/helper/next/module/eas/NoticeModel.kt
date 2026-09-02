package com.qust.helper.next.module.eas

import com.qust.helper.next.entity.eas.Notice
import com.qust.helper.next.network.client.EasHttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object NoticeModel {

	/**
	 * 查询教务通知
	 * @param page     第几页
	 * @param pageSize 每页数量
	 */
	suspend fun queryNotice(page: Int = 1, pageSize: Int = 20): List<Notice> {
		val json = EasHttpClient.post<FormDataContent, JsonObject>(
			url = "jwglxt/xtgl/index_cxDbsy.html",
			params = mapOf("doType" to "query"),
			body = FormDataContent(parameters {
				append("queryModel.showCount", pageSize.toString())
				append("queryModel.currentPage", page.toString())
				append("queryModel.sortName", "cjsj")
				append("queryModel.sortOrder", "desc")
			})
		)

		val item = json["items"]?.jsonArray ?: return emptyList()
		return List(item.size) { Notice.createFromJson(item[it].jsonObject) }
	}
}
