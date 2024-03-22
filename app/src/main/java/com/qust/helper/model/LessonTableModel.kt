package com.qust.helper.model

import com.qust.helper.model.account.EASAccount
import com.qust.helper.data.api.QustApi
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.DateUtils
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.util.Date
import java.util.regex.Pattern


/**
 * 课表功能模块
 */
object LessonTableModel {

	/**
	 * 匹配学年信息
	 */
	private val TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})\\)")

	private val WEEK_TABLE_MATCHER = Pattern.compile("<tr class=\"tab-th-2\">(.*?)</tr>", Pattern.DOTALL)
	private val WEEK_MATCHER = Pattern.compile("<th style=\"text-align: center\">(\\d+)</th>")
	private val DAY_MATCHER = Pattern.compile("<tbody>\\s+<tr>(.*?)</tr>", Pattern.DOTALL)
	private val DATE_MATCHER = Pattern.compile("<td id='(\\d{4}-\\d{2}-\\d{2})")

	private val XQHID_MATCHER = Pattern.compile("<select name=\"xqh_id\".*?</select>", Pattern.DOTALL)
	private val ZYHID_MATCHER = Pattern.compile("<select name=\"zyh_id\".*?</select>", Pattern.DOTALL)
	private val BHID_MATCHER = Pattern.compile("<select name=\"bh_id\".*?</select>", Pattern.DOTALL)

	private val OPTION_MATCHER = Pattern.compile("<option value=\"(.*?)\" selected=\"selected\">")

	/**
	 * 查询课表信息
	 * @param year 学年
	 * @param term 学期
	 */
	suspend fun queryLessonTable(easAccount: EASAccount, year: String, term: String): LessonTableQueryResult {
		val result: LessonTableQueryResult = getSchoolYearData(easAccount)
		try {
			easAccount.postNoCheck(
				QustApi.GET_LESSON_TABLE,
				FormBody.Builder()
					.add("xnm", year)
					.add("xqm", term)
					.add("kzlx", "ck")
					.build()
			).use { response ->
				val html: String = response.body!!.string()
				val js = JSONObject(html)
				if(!js.has("xsxx")) {
					result.error = "获取课表失败：该学年学期无您的注册信息"
				}else if(js.optString("xnxqsfkz").toBoolean()) {
					result.error = "获取课表失败：该学年学期课表当前时间段不允许查看"
				}else{
					val kblen: Int = js.getJSONArray("kbList").length()
					val sjklen: Int = js.getJSONArray("sjkList").length()
					val jxhjkclen: Int = js.getJSONArray("jxhjkcList").length()
					val xkkg: Boolean = js.optBoolean("xkkg", false) // 选课开关
					val jfckbkg: Boolean = js.optBoolean("jfckbkg", false) // 缴费查课表开关
					if(kblen == 0 && sjklen == 0 && jxhjkclen == 0 && xkkg && jfckbkg) {
						result.error = "获取课表失败：该学年学期尚无您的课表"
					} else if(!xkkg) {
						result.error = "获取课表失败：该学年学期的课表尚未开放"
					} else if(!jfckbkg) {
						result.error = "获取课表失败：缴费后可查询"
					} else if(!result.lessonTable.loadFromJson(js)) {
						result.error = "解析课表信息失败"
					}
				}
			}
		} catch(e: IOException) {
			result.error = "获取课表失败, 网络异常"
		} catch(e: Exception) {
			result.error = "获取课表失败"
		}
		return result
	}

	/**
	 * 查询课表信息备用方案
	 * @param entranceTime 入学年份（年级，有备用方案可以从html里获取）
	 * @param year 学年
	 * @param term 学期
	 */
	suspend fun queryClassLessonTable(easAccount: EASAccount, year: String, term: String): LessonTableQueryResult {

		val result: LessonTableQueryResult = getSchoolYearData(easAccount)

		try {
			easAccount.getNoCheck(QustApi.RECOMMENDED_LESSON_TABLE_PRINTING).use { response ->
				val html: String = response.body!!.string()

				// 从HTML里获取校区ID，专业号ID，班级ID
				val xqh_id = CodeUtils.matcher(XQHID_MATCHER, html)?.let{ CodeUtils.matcher(OPTION_MATCHER, it) } ?: throw CustomException("获取查询参数失败")
				val zyh_id = CodeUtils.matcher(ZYHID_MATCHER, html)?.let{ CodeUtils.matcher(OPTION_MATCHER, it) } ?: throw CustomException("获取查询参数失败")
				val bh_id = CodeUtils.matcher(BHID_MATCHER, html)?.let{ CodeUtils.matcher(OPTION_MATCHER, it) } ?: throw CustomException("获取查询参数失败")

				easAccount.postNoCheck(
					QustApi.GET_CLASS_LESSON_TABLE, FormBody.Builder()
						.add("tjkbzdm", "1")
						.add("tjkbzxsdm", "1")
						.add("xnm", year)
						.add("xqm", term)
						.add("njdm_id", easAccount.entranceTime.toString())
						.add("xqh_id", xqh_id)
						.add("zyh_id", zyh_id)
						.add("bh_id", bh_id)
						.build()
				).use {
					if(!result.lessonTable.loadFromJson(JSONObject(it.body!!.string()))) {
						result.error = "解析课表信息失败"
					}
				}
			}
		}catch (e: CustomException){
			result.error = e.message
		} catch(e: Exception) {
			result.error = "获取课表失败"
		}
		return result
	}

	/**
	 * 获取学年信息
	 */
	private suspend fun getSchoolYearData(
		account: EASAccount,
		result: LessonTableQueryResult = LessonTableQueryResult()
	): LessonTableQueryResult {
		try {
			// 从教务获取本学年信息
			val response = account.getNoCheck(QustApi.EA_YEAR_DATA).use { it.body!!.string() }

			// 学年信息
			var matcher = TIME_MATCHER.matcher(response)
			if(matcher.find()) {
				result.termText = matcher.group()

				result.lessonTable.startDay = try {
					DateUtils.YMD.parse(matcher.group(3)!!)!!
				} catch(_: Exception) { Date() }

				result.lessonTable.totalWeek = try {
					DateUtils.calcWeekOffset(result.lessonTable.startDay, DateUtils.YMD.parse(matcher.group(4)!!)!!)
				} catch(_: ParseException) { 1 }
			}

			// 根据校历查找开学日期
			matcher = WEEK_TABLE_MATCHER.matcher(response)
			if(matcher.find()) {
				val w = WEEK_MATCHER.matcher(matcher.group())
				if(!w.find()) return result
				var count = 0
				do {
					if("1" == w.group(1)) break
					count++
				} while(w.find())
				val m = DAY_MATCHER.matcher(response)
				if(m.find()) {
					var c = 0
					val d = DATE_MATCHER.matcher(m.group(1)!!)
					while(d.find()) {
						if(c++ == count) {
							result.lessonTable.startDay = DateUtils.YMD.parse(d.group(1)!!)!!
							break
						}
					}
				}
			}
		} catch(e: Exception) {
			Logger.e(e)
		}
		return result
	}

	class CustomException(msg: String): RuntimeException(msg)
}
