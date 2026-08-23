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

		/** 夏季时间表（14:00 上课） */
		val SUMMER = TimeTable(
			arrayOf(8_00, 9_00, 10_10, 11_10, 14_00, 15_00, 16_10, 17_10, 18_30, 19_30),
			arrayOf(8_50, 9_50, 11_00, 12_00, 14_50, 15_50, 17_00, 18_00, 19_20, 20_20)
		)

		/** 高密时间表 - 冬季 */
		val GAOMI_WINTER = TimeTable(
			arrayOf(8_30, 9_20, 10_25, 11_15, 13_30, 14_20, 15_25, 16_15, 18_00, 18_50),
			arrayOf(9_15, 10_05, 11_10, 12_00, 14_15, 15_05, 16_10, 17_00, 18_45, 19_35)
		)

		/** 高密时间表 - 夏季 */
		val GAOMI_SUMMER = TimeTable(
			arrayOf(8_30, 9_20, 10_25, 11_15, 14_00, 14_50, 15_55, 16_45, 18_30, 19_20),
			arrayOf(9_15, 10_05, 11_10, 12_00, 14_45, 15_35, 16_40, 17_30, 19_15, 20_05)
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
	 * 根据时间表计算一节课的时间位于时间表的第几节课
	 * 例如：
	 * [8:00 - 9:00, 9:00 - 10:00]
	 * (8:00 - 8:30) => (8 * 60, 8 * 60 + 30)       => (0.0, 0.5)
	 * (8:30 - 9:30) => (8 * 60 + 30, 9 * 60 + 30)  => (0.5, 1.5)
	 * @return 时间表的位置
	 */
	fun calcOffset(lesson: Lesson): Pair<Float, Float> {
		// 找开始的时间是时间表里的第几个，以小数表示不足一个的时间
		val st = lesson.startMinute
		var startOffset = 0F
		var s = 0
		while(s < sliceLength.size){
			if(st <= startMinute[s]){
				// 小于开始节点，即为第 s 个
				startOffset = s.toFloat()
				break
			}else if(st < endMinute[s]){
				// 小于结束节点，即为第 s + 多出的部分 个
				startOffset = s + (st - startMinute[s]) / sliceLength[s]
				break
			}else{
				// 大于结束节点的情况下继续查找下一个节点
			}
			s++
		}

		// 结束时间同理，从开始时间的位置往后找，防止无效查找
		val ed = lesson.endMinute
		var endOffset = count.toFloat()
		for(i in s ..< sliceLength.size){
			if(ed <= startMinute[i]){
				endOffset = i.toFloat()
				break
			}else if(ed < endMinute[i]){
				endOffset = (ed - startMinute[i]) / sliceLength[i] + i
				break
			}
		}
		return Pair(startOffset, endOffset)
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