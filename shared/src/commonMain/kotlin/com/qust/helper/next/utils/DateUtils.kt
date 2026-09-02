package com.qust.helper.next.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtils {

	val timeZone = TimeZone.currentSystemDefault()

	val YMD_HMS = LocalDateTime.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		day()
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
		day()
		char(' ')
		hour()
		char(':')
		minute()
	}

	val YMD_EEE_zh = LocalDate.Format {
		year()
		char('年')
		monthNumber()
		char('月')
		day()
		chars("日 ")
		dayOfWeek(DayOfWeekNames(listOf(
			"星期一",
			"星期二",
			"星期三",
			"星期四",
			"星期五",
			"星期六",
			"星期日",
		)))
	}

	val YMD = LocalDate.Format {
		year()
		char('-')
		monthNumber()
		char('-')
		day()
	}

	val YMD_zh = LocalDate.Format {
		year()
		char('年')
		monthNumber()
		char('月')
		day()
		char('日')
	}

	val MD = LocalDate.Format {
		monthNumber()
		char('-')
		day()
	}

	val HM = LocalDateTime.Format {
		hour()
		char(':')
		minute()
	}

	fun today() = Clock.System.now().toLocalDateTime().date

	fun currentDate() = Clock.System.now().toLocalDateTime().date

	fun currentTime() = Clock.System.now().toLocalDateTime()

	fun Instant.toLocalDateTime() = this.toLocalDateTime(timeZone)

	fun LocalDateTime.toInstant() = this.toInstant(timeZone)

	fun LocalDateTime.dayOfWeekText() = when(dayOfWeek){
		DayOfWeek.MONDAY -> "周一"
		DayOfWeek.TUESDAY -> "周二"
		DayOfWeek.WEDNESDAY -> "周三"
		DayOfWeek.THURSDAY -> "周四"
		DayOfWeek.FRIDAY -> "周五"
		DayOfWeek.SATURDAY -> "周六"
		DayOfWeek.SUNDAY -> "周日"
	}

	fun Long.epochMillisecondsToDateTime() = Instant.fromEpochMilliseconds(this).toLocalDateTime()

	fun Long.epochSecondsToDateTime() = Instant.fromEpochSeconds(this).toLocalDateTime(timeZone)

	fun LocalDateTime.to_YMD_HMS() = YMD_HMS.format(this)

	fun LocalDateTime.to_YMD_HM() = YMD_HM.format(this)

	fun Date.toLocalDate() = Instant.fromEpochMilliseconds(this.time).toLocalDateTime().date

	fun Date.toLocalDateTime() = Instant.fromEpochMilliseconds(this.time).toLocalDateTime()

	fun Calendar.toLocalDate() = this.time.toLocalDate()

	fun Calendar.toLocalDateTime() = this.time.toLocalDateTime()

	fun calcDayOffset(startTime: LocalDate, endTime: LocalDate): Int {
		return startTime.daysUntil(endTime)
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



