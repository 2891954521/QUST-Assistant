package com.qust.helper.model.eas

import com.qust.helper.data.QustApi
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.model.account.EasAccount
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.ui.theme.LESSON_BACKGROUND_COLORS
import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import com.qust.helper.utils.toLocalDateTime
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.util.regex.Pattern

object LessonQuery {

	private val XQHID_MATCHER = Pattern.compile("<select name=\"xqh_id\"(.*?)</select>", Pattern.DOTALL)
	private val ZYHID_MATCHER = Pattern.compile("<select name=\"zyh_id\"(.*?)</select>", Pattern.DOTALL)
	private val BHID_MATCHER = Pattern.compile("<select name=\"bh_id\"(.*?)</select>", Pattern.DOTALL)

	private val OPTION_MATCHER = Pattern.compile("<option value=\"(.*?)\" selected=\"selected\">")

	/**
	 * 匹配学年信息
	 */
	private val TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})\\)")

	/**
	 * 查询课表信息
	 * @param year 学年
	 * @param term 学期
	 */
	suspend fun queryLessonTable(easAccount: EasAccount, year: String, term: String): LessonTableQueryResult {
		val result: LessonTableQueryResult = getSchoolYearData(easAccount)
		try {
			val response = easAccount.post<String>(QustApi.GET_LESSON_TABLE){
				setBody(FormDataContent(parameters {
					append("xnm", year)
					append("xqm", term)
					append("kzlx", "ck")
				}))
			}
			val js = JsonUtils.json.parseToJsonElement(response).jsonObject
			if(!js.containsKey("xsxx")) {
				result.error = "获取课表失败：该学年学期无您的注册信息"

			}else if(js["xnxqsfkz", false]) {
				result.error = "获取课表失败：该学年学期课表当前时间段不允许查看"

			}else{
				val kblen: Int = js["kbList", JSONArray]?.size ?: 0
				val sjklen: Int = js["sjkList", JSONArray]?.size ?: 0
				val jxhjkclen: Int = js["jxhjkcList", JSONArray]?.size ?: 0

				val xkkg: Boolean = js["xkkg", false] // 选课开关
				val jfckbkg: Boolean = js["jfckbkg", false] // 缴费查课表开关

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
			result.error = "获取课表失败"
		}
		return result
	}

	/**
	 * 获取学年信息
	 */
	private suspend fun getSchoolYearData(account: EasAccount, result: LessonTableQueryResult = LessonTableQueryResult()): LessonTableQueryResult {
		try {
			// 从教务获取本学年信息
			val response: String = account.get(QustApi.EA_YEAR_DATA)

			// 学年信息
			val matcher = TIME_MATCHER.matcher(response)
			if(matcher.find()) {
				val startDay = try { DateUtils.YMD.parse(matcher.group(3)!!) } catch(_: Exception) { DateUtils.today() }
				val endDay = try { DateUtils.YMD.parse(matcher.group(4)!!) } catch(_: Exception){ DateUtils.today() }
				result.termText = matcher.group()
				result.startDay = startDay
				result.totalWeek = DateUtils.calcWeekOffset(startDay, endDay).coerceAtLeast(1)
			}
		} catch(e: Exception) {
			Logger.e(e = e)
		}
		return result
	}

	/**
	 * 从json中解析课表
	 */
	fun loadFromJson(json: JsonObject): List<Lesson>? {
		val timeTable = LessonTableRepository.currentLessonTable.value.timeTable
		return try {
			var index = 0
			val colors = mutableMapOf<String, Int>()
			val array = json["kbList", JSONArray] ?: return null

			return List(array.size){ i ->
				val js = array[i].jsonObject

				var weeks = 0L
				val times = js["zcd", "1周"].split(",")
				for(time in times) {
					var _time: String = time
					var type: Int  // 单双周类型
					if(_time.endsWith(")")){
						type = if(_time[_time.length - 2] == '单') 0 else 1
						_time = _time.substring(0, _time.length - 4)
					}else{
						type = -1
						_time = _time.substring(0, _time.length - 1)
					}

					if(_time.contains("-")) {
						val p = _time.split("-")
						var start = p[0].toInt() - 1
						val end = p[1].toInt()
						var mask: Long
						if(type == -1) {
							mask = (1L shl end - start) - 1 shl start
						} else {
							mask = 0L
							if(type == 0 && start % 2 == 1 || type == 1 && start % 2 == 0) start += 1
							var i = start
							while(i < end) {
								mask = mask or (1L shl i)
								i += 2
							}
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
				val color = if(j != -1){
					j % (LESSON_BACKGROUND_COLORS.size - 1) + 1
				}else{
					if(++index == LESSON_BACKGROUND_COLORS.size) index = 1
					colors[lessonID] = index
					index
				}

				Lesson(
					type = 0,
					lessonId = lessonID,
					colorLabel = color,
					weeks = weeks,
					week = js["xqj", 0],
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


/**
 * 查询课表完成后的结果
 * @param error 错误消息
 * @param termText 学期文本
 * @param lessonTable 课表
 */
data class LessonTableQueryResult(
	var error: String? = null,
	var termText: String = "",
	var startDay: LocalDate = Clock.System.now().toLocalDateTime().date,
	var totalWeek: Int = 1,
	var lessons: List<Lesson>? = null
)