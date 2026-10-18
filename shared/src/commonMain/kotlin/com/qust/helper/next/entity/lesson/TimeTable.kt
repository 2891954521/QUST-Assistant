package com.qust.helper.next.entity.lesson

/**
 * 时间表
 * @param startTime 上课时间
 * @param endTime 下课时间
 */
data class TimeTable(
	val startTime: Array<Int>,
	val endTime: Array<Int>
) {

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

	/** 一节课的时间 */
	val sliceLength: List<Float>

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

		sliceLength = List(count){ (endMinute[it] - startMinute[it]).toFloat() }
	}

	/**
	 * 把一节课按「节次」从本表映射到 [to] 表
	 *
	 * 规则：
	 * - 开始时间早于本表第一节 → 保持不动（返回 null）
	 * - 按本表节次下标定位，跨节连堂保留跨度
	 * - 目标节次超出 [to] 范围 → 保持不动（返回 null）
	 *
	 * @return [to] 表中的 (startMinute, endMinute)；null 表示保持不动
	 */
	fun mapLessonMinutes(lessonStart: Int, lessonEnd: Int, to: TimeTable): Pair<Int, Int>? {
		if(count == 0 || to.count == 0) return null
		if(lessonStart < startMinute.first()) return null

		// 本表中包含开始时间的节次
		val startIndex = startMinute.indexOfLast { it <= lessonStart }
		if(startIndex < 0 || startIndex > to.count - 1) return null

		// 结束时间按「严格小于下一节开始」定位，避免 lessonEnd 正好等于下一节开始时多算一节
		val endIndex = startMinute.indexOfLast { it < lessonEnd }.coerceAtLeast(startIndex)
		val targetEndIndex = endIndex.coerceAtMost(to.count - 1)

		return to.startMinute[startIndex] to to.endMinute[targetEndIndex]
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