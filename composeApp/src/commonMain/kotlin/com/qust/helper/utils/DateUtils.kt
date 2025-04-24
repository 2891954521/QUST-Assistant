package com.qust.helper.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

object DateUtils {

	val YMD_HMS = LocalDateTime.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		dayOfMonth()
		char(' ')
		hour()
		char(':')
		minute()
		char(':')
		second()
	}

	val YMD_HM = LocalDateTime.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		dayOfMonth()
		char(' ')
		hour()
		char(':')
		minute()
	}

	val YMD = LocalDateTime.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		dayOfMonth()
	}

	val MD = LocalDateTime.Format {
		monthNumber()
		char('-')
		dayOfMonth()
	}

	val HM = LocalDateTime.Format {
		hour()
		char(':')
		minute()
	}

	/**
	 * 计算时间差
	 */
//	fun timeDifference(s: String, e: String): String {
//		try {
//			val fromDate = YMD_HM.parse(s)
//			val toDate = YMD_HM.parse(e)
//			val from = fromDate.time
//			val to = toDate.time
//			val hours = ((to - from) / (1000 * 60 * 60)).toInt()
//			val minutes = ((to - from) / (1000 * 60)).toInt() % 60
//			return if(hours != 0) {
//				if(minutes == 0) {
//					hours.toString() + "小时"
//				} else {
//					hours.toString() + "小时" + minutes + "分钟"
//				}
//			} else {
//				minutes.toString() + "分钟"
//			}
//		} catch(ignored: Exception) {
//			return ""
//		}
//	}

	fun calcDayOffset(startTime: Instant, endTime: Instant): Long {
		return startTime.minus(endTime).inWholeDays
	}

	fun calcWeekOffset(startTime: Instant, endTime: Instant): Long {
		val dayOfWeek = startTime.toLocalDateTime(TimeZone.UTC).dayOfWeek.isoDayNumber
		val dayOffset = calcDayOffset(startTime, endTime)
		return dayOffset / 7 + if(dayOffset > 0) {
			if((dayOffset % 7 + dayOfWeek) > 7) 1 else 0
		} else {
			if((dayOffset % 7 + dayOfWeek) < 1) -1 else 0
		}
	}
}