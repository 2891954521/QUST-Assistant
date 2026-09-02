package com.qust.helper.next.module.eas

import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.next.common.json.get
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.network.client.EasHttpClient
import com.qust.helper.next.network.entity.lesson.LessonTableQueryResult
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.theme.color.Colors.LESSON_BACKGROUND_COLORS
import com.qust.helper.next.utils.DateUtils
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object LessonModel {

	/**
	 * 匹配学年信息
	 */
	private val TIME_MATCHER = Regex(
		"""([0-9]{4}-[0-9]{4})学年([0-9])学期\((\d{4}-\d{2}-\d{2})至(\d{4}-\d{2}-\d{2})\)"""
	)

	/**
	 * 查询课表信息
	 * @param year 学年
	 * @param term 学期
	 */
	suspend fun queryLessonTable(year: String, term: String): LessonTableQueryResult {
		val result = getSchoolYearData()
		try {
			val js = EasHttpClient.post<FormDataContent, JsonObject>(
				url = "jwglxt/kbcx/xskbcx_cxXsgrkb.html",
				body = FormDataContent(parameters {
					append("xnm", year)
					append("xqm", term)
					append("kzlx", "ck")
				})
			)

			if(!js.containsKey("xsxx")) {
				result.error = "获取课表失败：该学年学期无您的注册信息"
			} else if(js["xnxqsfkz", false]) {
				result.error = "获取课表失败：该学年学期课表当前时间段不允许查看"
			} else {
				val kblen = js["kbList"]?.jsonArray?.size ?: 0
				val sjklen = js["sjkList"]?.jsonArray?.size ?: 0
				val jxhjkclen = js["jxhjkcList"]?.jsonArray?.size ?: 0

				val xkkg = js["xkkg", false]
				val jfckbkg = js["jfckbkg", false]

				if(kblen == 0 && sjklen == 0 && jxhjkclen == 0 && xkkg && jfckbkg) {
					result.error = "获取课表失败：该学年学期尚无您的课表"
				} else if(!xkkg) {
					result.error = "获取课表失败：该学年学期的课表尚未开放"
				} else if(!jfckbkg) {
					result.error = "获取课表失败：缴费后可查询"
				} else {
					result.lessons = loadFromJson(js)
					if(result.lessons == null) result.error = "解析课表信息失败"
				}
			}
		} catch(e: Exception) {
			Logger.e("queryLessonTable", e)
			result.error = "获取课表失败"
		}
		return result
	}

	/**
	 * 获取学年信息
	 */
	private suspend fun getSchoolYearData(result: LessonTableQueryResult = LessonTableQueryResult()): LessonTableQueryResult {
		try {
			val response: String = EasHttpClient.executeAsString(
				EasHttpClient.buildGetRequest(
					url = "jwglxt/xtgl/index_cxAreaFive.html",
					params = mapOf(
						"localeKey" to "zh_CN",
						"gnmkdm" to "index"
					)
				)
			)
			val match = TIME_MATCHER.find(response)
			if(match != null) {
				val startDay = try {
					DateUtils.YMD.parse(match.groupValues[3])
				} catch(_: Exception) {
					DateUtils.today()
				}
				val endDay = try {
					DateUtils.YMD.parse(match.groupValues[4])
				} catch(_: Exception) {
					DateUtils.today()
				}
				result.termText = match.value
				result.startDay = startDay
				result.totalWeek = DateUtils.calcWeekOffset(startDay, endDay).coerceAtLeast(1)
			}
		} catch(e: Exception) {
			Logger.e("getSchoolYearData", e)
		}
		return result
	}

	/**
	 * 从 json 中解析课表
	 */
	fun loadFromJson(json: JsonObject): List<Lesson>? {
		val timeTable = LessonTableRepository.currentLessonTable.value.timeTable
		return try {
			var index = 0
			val colors = mutableMapOf<String, Int>()
			val array = json["kbList"]?.jsonArray ?: return null

			List(array.size) { i ->
				val js = array[i].jsonObject

				var weeks = 0L
				val times = js["zcd", "1周"].split(",")
				for(time in times) {
					var _time = time
					val type: Int
					if(_time.endsWith(")")) {
						type = if(_time[_time.length - 2] == '单') 0 else 1
						_time = _time.substring(0, _time.length - 4)
					} else {
						type = -1
						_time = _time.substring(0, _time.length - 1)
					}

					if(_time.contains("-")) {
						val p = _time.split("-")
						var start = p[0].toInt() - 1
						val end = p[1].toInt()
						val mask: Long
						if(type == -1) {
							mask = (1L shl end - start) - 1 shl start
						} else {
							var m = 0L
							if(type == 0 && start % 2 == 1 || type == 1 && start % 2 == 0) start += 1
							var j = start
							while(j < end) {
								m = m or (1L shl j)
								j += 2
							}
							mask = m
						}
						weeks = weeks xor mask
					} else {
						weeks = weeks or (1L shl (_time.toInt() - 1))
					}
				}

				val sp = js["jcs", ""].split("-")
				val start = timeTable.startMinute[sp[0].toInt() - 1]
				val end = timeTable.endMinute[sp[1].toInt() - 1]

				val lessonID = js["kch", ""]
				val j = colors[lessonID] ?: -1
				val color = if(j != -1) {
					j % (LESSON_BACKGROUND_COLORS.size - 1) + 1
				} else {
					if(++index == LESSON_BACKGROUND_COLORS.size) index = 1
					colors[lessonID] = index
					index
				}

				Lesson(
					type = 0,
					lessonId = lessonID,
					colorLabel = color,
					weeks = weeks,
					week = (js["xqj", 1] - 1).coerceAtLeast(0),
					startMinute = start,
					endMinute = end,
					name = js["kcmc", ""].trim(),
					place = js["cdmc", ""].trim(),
					teacher = js["xm", ""].trim(),
				)
			}
		} catch(e: Exception) {
			Logger.e(json.toString(), e)
			null
		}
	}
}
