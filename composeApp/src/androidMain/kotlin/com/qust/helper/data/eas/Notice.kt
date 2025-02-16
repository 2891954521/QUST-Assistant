package com.qust.helper.data.eas

import kotlinx.serialization.Serializable
import org.json.JSONObject


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
		fun createFromJson(js: JSONObject): Notice {
			return Notice(
				id = if(js.has("id")) js.getString("id") else "",
				time = if(js.has("cjsj")) js.getString("cjsj") else "",
				content = if(js.has("xxnr")) js.getString("xxnr") else ""
			)
		}
	}
}
