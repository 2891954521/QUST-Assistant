package com.qust.helper.model.eas

import com.qust.helper.data.QustApi
import com.qust.helper.entity.eas.Exam
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.database.ExamStorage
import com.qust.helper.model.database.getExamStorage
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object ExamModel: ExamStorage by getExamStorage() {

	/**
	 * 查询考试
	 * @param xnm  学年代码 20xx
	 * @param xqm  学期代码 12 | 3
	 */
	suspend fun queryExam(account: EasAccount, term: Int, xnm: String, xqm: String): List<Exam> {
		val json = account.post<String>(QustApi.GET_EXAM) {
			setBody(FormDataContent(parameters {
				append("xnm", xnm)
				append("xqm", xqm)
				append("queryModel.showCount", "999")
			}))
		}

		val item = JsonUtils.parseString<JsonObject>(json)["items", JSONArray] ?: return emptyList()

		return List(item.size) {
			val js = item[it].jsonObject
			Exam(
				term = term,
				name = js["kcmc", ""],
				place = js["kssj", ""],
				time = js["cdmc", ""],
			)
		}
	}

}