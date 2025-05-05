package com.qust.helper.entity.eas

import com.qust.helper.utils.JsonUtils.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 教务系统消息
 * @param id ID
 * @param time 创建时间
 * @param content 消息内容
 */
@Serializable
data class Notice(
	val id: String = "",
	val time: String = "",
	val content: String = "",
){
	companion object {
		fun createFromJson(js: JsonObject): Notice {
			return Notice(
				id = js["id", ""],
				time = js["cjsj", ""],
				content = js["xxnr", ""]
			)
		}
	}
}