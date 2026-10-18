package com.qust.helper.next.entity.lesson

/**
 * 时间表校验结果
 *
 * @param periodIndex 出错节次（0 起），-1 表示整体性错误
 * @param message 面向用户的提示
 */
data class TimeTableIssue(
	val periodIndex: Int,
	val message: String,
)

/**
 * 时间表校验规则
 */
object TimeTableValidator {

	const val MIN_PERIODS = 1
	const val MAX_PERIODS = 20

	/**
	 * 校验一份时间表
	 * @return null 表示通过
	 */
	fun validate(startTime: List<Int>, endTime: List<Int>): TimeTableIssue? {
		if(startTime.size != endTime.size) {
			return TimeTableIssue(-1, "开始时间与结束时间的节数不一致")
		}
		if(startTime.size !in MIN_PERIODS..MAX_PERIODS) {
			return TimeTableIssue(-1, "节数需在 $MIN_PERIODS~$MAX_PERIODS 之间")
		}

		startTime.forEachIndexed { i, value ->
			if(!isValidTime(value)) return TimeTableIssue(i, "第 ${i + 1} 节开始时间不正确")
		}
		endTime.forEachIndexed { i, value ->
			if(!isValidTime(value)) return TimeTableIssue(i, "第 ${i + 1} 节结束时间不正确")
		}

		for(i in startTime.indices) {
			val start = toMinute(startTime[i])
			val end = toMinute(endTime[i])
			if(end <= start) {
				return TimeTableIssue(i, "第 ${i + 1} 节结束时间需晚于开始时间")
			}
			if(i > 0) {
				// 上一节已校验通过，因此 endTime[i - 1] 必然大于 startTime[i - 1]
				if(start < toMinute(endTime[i - 1])) {
					return TimeTableIssue(i, "第 ${i + 1} 节与上一节时间重叠")
				}
			}
		}
		return null
	}

	/** HHMM 是否合法（00:00 ~ 23:59） */
	fun isValidTime(value: Int): Boolean {
		val hour = value / 100
		val minute = value % 100
		return hour in 0..23 && minute in 0..59
	}

	/** HHMM → 当天分钟数；非法时间返回 -1 */
	fun toMinute(value: Int): Int {
		if(!isValidTime(value)) return -1
		return (value / 100) * 60 + value % 100
	}
}
