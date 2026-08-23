package com.qust.helper.entity.business

import kotlinx.serialization.Serializable

/**
 * 电费充值 - 宿舍电控信息
 */
@Serializable
data class Electricity(
	val id: Array<String> = emptyArray(),
	val name: Array<String> = emptyArray(),
	val roomName: String = "",
	var balance: Float = Float.NaN,
) {
	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as Electricity

		if(!id.contentEquals(other.id)) return false
		if(!name.contentEquals(other.name)) return false
		if(roomName != other.roomName) return false
		return balance == other.balance
	}

	override fun hashCode(): Int {
		var result = id.contentHashCode()
		result = 31 * result + name.contentHashCode()
		result = 31 * result + roomName.hashCode()
		result = 31 * result + balance.hashCode()
		return result
	}
}

/**
 * 电控节点
 */
@Serializable
class Node(
	val nodeId: String = "",
	val name: String = "",
	var child: Array<Node> = emptyArray()
)
