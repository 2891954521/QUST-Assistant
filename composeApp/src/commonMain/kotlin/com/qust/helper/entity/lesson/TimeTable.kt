package com.qust.helper.entity.lesson

/**
 * 时间表
 * @param startTime 上课时间
 * @param endTime 下课时间
 */
data class TimeTable(
	val startTime: Array<Int>,
	val endTime: Array<Int>
) {

	companion object {
		val DEFAULT = TimeTable(
			arrayOf(8_00, 9_00, 10_10, 11_10, 13_30, 14_30, 15_40, 16_40, 18_30, 19_30),
			arrayOf(8_50, 9_50, 11_00, 12_00, 14_20, 15_20, 16_30, 17_30, 19_20, 20_20)
		)
	}

	val startTimeStr: List<String>
	val endTimeStr: List<String>

	init {
		if(startTime.size != endTime.size) {
			throw RuntimeException("LessonTable startTime length most equal endTime length")
		}
		startTimeStr = startTime.map {
			val hour = it / 100
			val minute = it % 100
			"${if(hour < 10) "0${hour}" else hour}:${if(minute < 10) "0${minute}" else minute}"
		}.toList()
		endTimeStr = endTime.map {
			val hour = it / 100
			val minute = it % 100
			"${if(hour < 10) "0${hour}" else hour}:${if(minute < 10) "0${minute}" else minute}"
		}.toList()
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as TimeTable

		if(!startTime.contentEquals(other.startTime)) return false
		if(!endTime.contentEquals(other.endTime)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = startTime.contentHashCode()
		result = 31 * result + endTime.contentHashCode()
		return result
	}
}