package com.qust.helper.next.entity.eas

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 考试安排
 * @param term  第几学期
 * @param name  课程名称
 * @param place 考试地点
 * @param time  考试时间
 */
@Immutable
@Serializable
data class Exam(
	val id: Int = 0,

	val term: Int = 0,

	val name: String = "",
	val place: String = "",
	val time: String = "",

	val isNew: Int = 1,
) {

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(other !is Exam) return false

		if(term != other.term) return false
		if(name != other.name) return false
		if(place != other.place) return false
		if(time != other.time) return false

		return true
	}

	override fun hashCode(): Int {
		var result = term
		result = 31 * result + name.hashCode()
		result = 31 * result + place.hashCode()
		result = 31 * result + time.hashCode()
		return result
	}
}
