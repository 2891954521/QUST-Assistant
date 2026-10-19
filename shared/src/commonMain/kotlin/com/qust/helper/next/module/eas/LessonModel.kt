package com.qust.helper.next.module.eas

import com.qust.helper.next.common.json.get
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
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
	fun loadFromJson(
		json: JsonObject,
		timeTable: TimeTable = LessonTableRepository.currentLessonTable.value.timeTable,
	): List<Lesson>? {
		if(timeTable.count == 0) return null
		return try {
			var index = 0
			val colors = mutableMapOf<String, Int>()
			val array = json["kbList"]?.jsonArray ?: return null

			List(array.size) { i ->
				val js = array[i].jsonObject

				val weeks = parseWeeks(js["zcd", "第1周"])

				// 节次 → 时间：教务可能返回超出当前时间表的节次（例如 10 节配置遇到 11 节课），
				// 夹取到表内，避免整张课表解析失败
				val sp = js["jcs", ""].split("-")
				val count = timeTable.count
				val startIndex = (sp.getOrNull(0)?.toIntOrNull() ?: 1).coerceIn(1, count)
				val endIndex = (sp.getOrNull(1)?.toIntOrNull() ?: startIndex).coerceIn(startIndex, count)
				val start = timeTable.startMinute[startIndex - 1]
				val end = timeTable.endMinute[endIndex - 1]

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

	/**
	 * 解析 zcd（周次段）为周次位掩码，bit0 对应第 1 周
	 *
	 * 支持的格式：
	 * 只有一周上课：第x周
	 * 连续多周：x-x周
	 * 间断多周：x-x周,x周,x-x周
	 * 单双周：x-x周(单),x-x周(双)
	 */
	fun parseWeeks(zcd: String): Long {
		var weeks = 0L

		// "第"、"周"只起修饰/分隔作用，先整体去掉再逐段解析，
		// 这样 第3周 / 1-16周 / 1,2,3周 都能正确处理
		val normalized = zcd.replace("第", "").replace("周", "")
		for(segment in normalized.split(",", "，", "、")) {
			val seg = segment.trim()
			if(seg.isEmpty()) continue

			// 单双周：(单) 取奇数周，(双) 取偶数周，无标记则是整段连续周
			var type = -1
			var body = seg
			val cut = seg.indexOfAny(charArrayOf('(', '（'))
			if(cut >= 0) {
				val flag = seg.substring(cut)
				type = when {
					flag.contains('单') -> 0
					flag.contains('双') -> 1
					else -> -1
				}
				body = seg.substring(0, cut).trim()
			}

			// 单周 "3"、连续 "1-16"、异常 "1-"（按单周处理）
			val point = body.split("-")
			val first = point.getOrNull(0)?.trim()?.toIntOrNull() ?: continue
			val last = point.getOrNull(1)?.trim()?.toIntOrNull() ?: first
			val from = first.coerceIn(1, MAX_WEEK)
			val to = last.coerceIn(from, MAX_WEEK)

			var start = from - 1
			val mask: Long
			if(type == -1) {
				// 连续周次：bit[start, to) 全部置位
				mask = ((1L shl (to - start)) - 1L) shl start
			} else {
				// 单双周：先把起点对齐到目标奇偶性，再每隔 2 周置位
				if(type == 0 && start % 2 == 1 || type == 1 && start % 2 == 0) start += 1
				var m = 0L
				var j = start
				while(j < to) {
					m = m or (1L shl j)
					j += 2
				}
				mask = m
			}
			// 多段之间取并集：同一周被重复描述时不能互相抵消
			weeks = weeks or mask
		}
		return weeks
	}

	/**
	 * 位掩码最多能表示 [1, 63] 周（Long 只有 63 个可用位）
	 */
	private const val MAX_WEEK = 63
}
