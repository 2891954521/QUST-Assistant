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

	/** 一天有几节课 */
	val count: Int

	/**
	 * 字符串格式的时间
	 */
	val startTimeStr: List<String>
	val endTimeStr: List<String>

	/**
	 * 从 0 点开始到当前时间点的分钟数
	 */
	val startMinute: List<Int>
	val endMinute: List<Int>

	init {
		if(startTime.size != endTime.size) {
			throw RuntimeException("LessonTable startTime length most equal endTime length")
		}

		count = startTime.size

		val startMinute = mutableListOf<Int>()
		val endMinute = mutableListOf<Int>()
		val startTimeStr = mutableListOf<String>()
		val endTimeStr = mutableListOf<String>()

		for(i in startTime.indices){
			val st = startTime[i]
			val shour = st / 100
			val sminute = st % 100

			val ed = endTime[i]
			val ehour = ed / 100
			val eminute = ed % 100

			startMinute.add(shour * 60 + sminute)
			endMinute.add(ehour * 60 + eminute)

			startTimeStr.add("${if(shour < 10) "0${shour}" else shour}:${if(sminute < 10) "0${sminute}" else sminute}")
			endTimeStr.add("${if(ehour < 10) "0${ehour}" else ehour}:${if(eminute < 10) "0${eminute}" else eminute}")
		}

		this.startTimeStr = startTimeStr.toList()
		this.endTimeStr = endTimeStr.toList()
		this.startMinute = startMinute.toList()
		this.endMinute = endMinute.toList()
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