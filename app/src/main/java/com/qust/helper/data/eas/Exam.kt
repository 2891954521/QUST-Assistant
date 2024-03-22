package com.qust.helper.data.eas

import kotlinx.serialization.Serializable
import org.json.JSONObject


/**
 * 考试安排
 * @param name  课程名称
 * @param place 考试地点
 * @param time  考试时间
 */
@Serializable
data class Exam(
	val name: String = "",
	val place: String = "",
	val time: String = "",
) {

	companion object {
		fun createFromJson(js: JSONObject): Exam {
			return Exam(
				name = if(js.has("kcmc")) js.getString("kcmc") else "",
				place = if(js.has("kssj")) js.getString("kssj") else "",
				time = if(js.has("cdmc")) js.getString("cdmc") else ""
			)
		}
	}
}