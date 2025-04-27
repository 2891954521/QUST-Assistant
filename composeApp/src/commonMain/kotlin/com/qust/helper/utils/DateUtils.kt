package com.qust.helper.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
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

	val YMD = LocalDate.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		dayOfMonth()
	}

	val MD = LocalDate.Format {
		monthNumber()
		char('-')
		dayOfMonth()
	}

	val HM = LocalDateTime.Format {
		hour()
		char(':')
		minute()
	}

	fun today() = Clock.System.now().toLocalDateTime().date

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

	fun calcDayOffset(startTime: LocalDate, endTime: LocalDate): Int {
		val offset = endTime.minus(startTime)
		return offset.days + offset.months * 30
	}

	fun calcWeekOffset(startTime: LocalDate, endTime: LocalDate): Int {
		val dayOfWeek = startTime.dayOfWeek.isoDayNumber
		val dayOffset = calcDayOffset(startTime, endTime)
		return dayOffset / 7 + if(dayOffset > 0) {
			if((dayOffset % 7 + dayOfWeek) > 7) 1 else 0
		} else {
			if((dayOffset % 7 + dayOfWeek) < 1) -1 else 0
		}
	}
}

fun Instant.toLocalDateTime() = this.toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.toInstant() = this.toInstant(TimeZone.UTC)


