package com.qust.helper.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.api.QustApi
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.model.account.EASAccount
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.DateUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import okhttp3.FormBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern


/**
 * 课表功能模块
 */
object LessonTableRepository {

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
	 * 当前时间表
	 */
	var currentTimeTable by mutableIntStateOf(Setting.getInt(Keys.KEY_TIME_TABLE, 0)); private set
	fun setTimeTableValue(value: Int) {
		currentTimeTable = value
		Setting.edit { it.putInt(Keys.KEY_TIME_TABLE, value) }
	}

	/**
	 * 显示所有课程
	 */
	val _showAllLesson = mutableStateOf(Setting.getBoolean(Keys.KEY_SHOW_ALL_LESSON, true))
	val showAllLesson by _showAllLesson
	fun setShowAllLessonValue(value: Boolean) {
		_showAllLesson.value = value
		Setting.edit { it.putBoolean(Keys.KEY_SHOW_ALL_LESSON, value) }
	}

	/**
	 * 隐藏已结课课程
	 */
	val _hideFinishLesson = mutableStateOf(Setting.getBoolean(Keys.KEY_HIDE_FINISH_LESSON, false))
	val hideFinishLesson by _hideFinishLesson
	fun setHideFinishLessonValue(value: Boolean) {
		_hideFinishLesson.value = value
		Setting.edit { it.putBoolean(Keys.KEY_HIDE_FINISH_LESSON, value) }
	}

	/**
	 * 隐藏教师
	 */
	val _hideTeacher = mutableStateOf(Setting.getBoolean(Keys.KEY_HIDE_TEACHER, true))
	val hideTeacher by _hideTeacher
	fun setHideTeacherValue(value: Boolean) {
		_hideTeacher.value = value
		Setting.edit { it.putBoolean(Keys.KEY_HIDE_TEACHER, value) }
	}

	/**
	 * 开学时间, 和LessonTable是同步更新的
	 */
	val _startDay = mutableStateOf(Date())
	val startDay by _startDay
	fun setStartDayValue(value: Date){
		_startDay.value = value
		lessonTable.startDay = value
		saveLessonTable()
	}

	/**
	 * 开学时间, 和LessonTable是同步更新的
	 */
	val _totalWeek = mutableIntStateOf(1)
	val totalWeek by _totalWeek
	fun setTotalWeekValue(value: Int){
		_totalWeek.intValue = value
		lessonTable.totalWeek = value
		saveLessonTable()
	}

	/**
	 * 当前周 (从1开始)
	 */
	var currentWeek = mutableIntStateOf(1)

	/**
	 * 当前星期 ( 0-6, 周一 —— 周日)
	 */
	var dayOfWeek = mutableIntStateOf(0)

	val _lessonTable = mutableStateOf(LessonTable())
	val lessonTable by _lessonTable

	init {
		loadLesson()
		updateDate()
	}

	/**
	 * 更新日期信息
	 */
	fun updateDate() {
		val currentDay: Calendar = Calendar.getInstance().also { it.firstDayOfWeek = Calendar.MONDAY }
		val startDay: Calendar = (currentDay.clone() as Calendar).also { it.time = startDay }

		dayOfWeek.intValue = currentDay[Calendar.DAY_OF_WEEK].let { if(it == Calendar.SUNDAY) 6 else it - 2 }
		currentWeek.intValue = (currentDay[Calendar.WEEK_OF_YEAR] - startDay[Calendar.WEEK_OF_YEAR] + 1).coerceAtLeast(1)
	}

	/**
	 * 从本地文件初始化课表
	 */
	@OptIn(ExperimentalSerializationApi::class)
	private fun loadLesson() {
		if(Setting.lessonTableFolder.exists()) {
			Setting.lessonTableFolder.listFiles()?.let{
				for(file in it){
					if(file?.exists() == true) {
						try{
							FileInputStream(file).use { stream ->
								_lessonTable.value = Json.decodeFromStream<LessonTable>(stream)
								_startDay.value = lessonTable.startDay
								_totalWeek.intValue = lessonTable.totalWeek
							}
							break
						}catch(_: Exception) { }
					}
				}
			}
		} else {
			Setting.lessonTableFolder.mkdirs()
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun saveLessonTable(newLessonTable: LessonTable? = null): Boolean{
		if(newLessonTable != null){
			_lessonTable.value = newLessonTable
		}

		// 去除上课周数为0的课程
		val lessonGroups: Array<Array<LessonGroup?>> = lessonTable.lessons
		for(dailyLesson in lessonGroups) {
			for(timeSlot in dailyLesson.indices) {
				val group: LessonGroup = dailyLesson[timeSlot] ?: continue
				if(group.lessons.isEmpty()){
					dailyLesson[timeSlot] = null
					continue
				}
				group.lessons = group.lessons.filter { it.week != 0L }.toTypedArray()
			}
		}

		_startDay.value = lessonTable.startDay
		_totalWeek.intValue = lessonTable.totalWeek

		if(!Setting.lessonTableFolder.exists()) Setting.lessonTableFolder.mkdirs()
		val file = File(Setting.lessonTableFolder, "lessonTable")
		return try{
			FileOutputStream(file).use { stream ->
				Json.encodeToStream<LessonTable>(lessonTable, stream)
			}
			true
		}catch(_: Exception) {
			false
		}
	}


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
