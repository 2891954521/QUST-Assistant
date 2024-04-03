package com.qust.helper.data

import kotlinx.serialization.Serializable

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