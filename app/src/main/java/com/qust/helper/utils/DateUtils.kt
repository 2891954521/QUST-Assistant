package com.qust.helper.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

	val YMD_HMS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

	val YMD_HM = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

	val YMD = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

	val MD = SimpleDateFormat("MM-dd", Locale.CHINA)

	val HM = SimpleDateFormat("HH:mm", Locale.CHINA)


	@Synchronized
	fun getDateString(c: Calendar): String {
		return YMD_HM.format(c.time)
	}

	fun getDateString(s: String): Date {
		return try {
			YMD_HM.parse(s)!!
		} catch(_: Exception) {
			Date()
		}
	}

	/**
	 * 计算时间差
	 */
	fun timeDifference(s: String, e: String): String {
		try {
			val fromDate = YMD_HM.parse(s)!!
			val toDate = YMD_HM.parse(e)!!
			val from = fromDate.time
			val to = toDate.time
			val hours = ((to - from) / (1000 * 60 * 60)).toInt()
			val minutes = ((to - from) / (1000 * 60)).toInt() % 60
			return if(hours != 0) {
				if(minutes == 0) {
					hours.toString() + "小时"
				} else {
					hours.toString() + "小时" + minutes + "分钟"
				}
			} else {
				minutes.toString() + "分钟"
			}
		} catch(ignored: Exception) {
			return ""
		}
	}

	fun calcDayOffset(date1: Date, date2: Date): Int {
		val cal1 = Calendar.getInstance()
		cal1.time = date1
		val cal2 = Calendar.getInstance()
		cal2.time = date2
		val day1 = cal1[Calendar.DAY_OF_YEAR]
		val day2 = cal2[Calendar.DAY_OF_YEAR]
		val year1 = cal1[Calendar.YEAR]
		val year2 = cal2[Calendar.YEAR]
		return if(year1 != year2) {  //同一年
			var timeDistance = 0
			for(i in year1 until year2) {
				timeDistance += if(i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {  //闰年
					366
				} else {  //不是闰年
					365
				}
			}
			timeDistance + (day2 - day1)
		} else { //不同年
			day2 - day1
		}
	}

	fun calcWeekOffset(startTime: Date, endTime: Date): Int {
		val cal = Calendar.getInstance()
		cal.time = startTime
		var dayOfWeek = cal[Calendar.DAY_OF_WEEK]
		dayOfWeek -= 1
		if(dayOfWeek == 0) dayOfWeek = 7
		val dayOffset = calcDayOffset(startTime, endTime)
		var weekOffset = dayOffset / 7
		val a: Int = if(dayOffset > 0) {
			if(dayOffset % 7 + dayOfWeek > 7) 1 else 0
		} else {
			if(dayOfWeek + dayOffset % 7 < 1) -1 else 0
		}
		weekOffset += a
		return weekOffset
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Serializer(forClass = Date::class)
	object DateSerializer {
		override fun deserialize(decoder: Decoder): Date {
			val dateString = decoder.decodeString()
			return YMD.parse(dateString) ?: Date()
		}

		override fun serialize(encoder: Encoder, value: Date) {
			val dateString = YMD.format(value)
			encoder.encodeString(dateString)
		}
	}
}
