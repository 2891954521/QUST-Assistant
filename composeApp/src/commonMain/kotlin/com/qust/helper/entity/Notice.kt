package com.qust.helper.entity

import kotlinx.serialization.Serializable

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

}
