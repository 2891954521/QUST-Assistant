package com.qust.helper.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Keys
import com.qust.helper.data.QustApi
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.model.account.EASAccount
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.SettingUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import okhttp3.FormBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern


/**
 * 课表功能模块
 */
object LessonTableRepository {

	private val XQHID_MATCHER = "<select name=\"xqh_id\"(.*?)</select>".toRegex(RegexOption.DOT_MATCHES_ALL)
	private val ZYHID_MATCHER = "<select name=\"zyh_id\"(.*?)</select>".toRegex(RegexOption.DOT_MATCHES_ALL)
	private val BHID_MATCHER = "<select name=\"bh_id\"(.*?)</select>".toRegex(RegexOption.DOT_MATCHES_ALL)

	private val OPTION_MATCHER = "<option value=\"(.*?)\" selected=\"selected\">".toRegex()

	/**
	 * 当前时间表
	 */
	val _currentTimeTable = mutableIntStateOf(SettingUtils.getInt(Keys.KEY_TIME_TABLE, 0))
	val currentTimeTable by mutableIntStateOf(SettingUtils.getInt(Keys.KEY_TIME_TABLE, 0))
	fun setTimeTableValue(value: Int) {
		_currentTimeTable.value = value
		SettingUtils.putInt(Keys.KEY_TIME_TABLE, value)
	}

	//使用高密时间表
	val _gaomiTimeTable = mutableStateOf(SettingUtils.getBoolean(Keys.KEY_GAOMI_TIME_TABLE, false))
	val gaomiTimeTable by _gaomiTimeTable
	fun setGaomiTimeTable(value: Boolean) {
		_gaomiTimeTable.value = value
		SettingUtils.putBoolean(Keys.KEY_GAOMI_TIME_TABLE, value)
	}

	/**
	 * 显示所有课程
	 */
	val _showAllLesson = mutableStateOf(SettingUtils.getBoolean(Keys.KEY_SHOW_ALL_LESSON, true))
	val showAllLesson by _showAllLesson
	fun setShowAllLessonValue(value: Boolean) {
		_showAllLesson.value = value
		SettingUtils.putBoolean(Keys.KEY_SHOW_ALL_LESSON, value)
	}

	/**
	 * 隐藏已结课课程
	 */
	val _hideFinishLesson = mutableStateOf(SettingUtils.getBoolean(Keys.KEY_HIDE_FINISH_LESSON, false))
	val hideFinishLesson by _hideFinishLesson
	fun setHideFinishLessonValue(value: Boolean) {
		_hideFinishLesson.value = value
		SettingUtils.putBoolean(Keys.KEY_HIDE_FINISH_LESSON, value)
	}

	/**
	 * 隐藏教师
	 */
	val _hideTeacher = mutableStateOf(SettingUtils.getBoolean(Keys.KEY_HIDE_TEACHER, true))
	val hideTeacher by _hideTeacher
	fun setHideTeacherValue(value: Boolean) {
		_hideTeacher.value = value
		SettingUtils.putBoolean(Keys.KEY_HIDE_TEACHER, value)
	}

	/**
	 * 锁定课表
	 */
	var _lockLesson = mutableStateOf(SettingUtils.getBoolean(Keys.KEY_LOCK_LESSON, false))
	val lockLesson by _lockLesson
	fun setLockLessonValue(value: Boolean) {
		_lockLesson.value = value
		SettingUtils.putBoolean(Keys.KEY_LOCK_LESSON, value)
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
		if(SettingModel.lessonTableFolder.exists()) {
			SettingModel.lessonTableFolder.listFiles()?.let{
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
			SettingModel.lessonTableFolder.mkdirs()
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

		if(!SettingModel.lessonTableFolder.exists()) SettingModel.lessonTableFolder.mkdirs()
		val file = File(SettingModel.lessonTableFolder, "lessonTable")
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
			var response: String
			easAccount.getNoRedirect(QustApi.EA_MAIN_MENU).use { response = it.body!!.string() }

			// 从教务主页面获取跳转的URL和参数
			var gnmkdm = "0"
			var url = QustApi.RECOMMENDED_LESSON_TABLE_PRINTING
			val matcher = "\\('([A-Za-z0-9]+)','([A-Za-z0-9/._]*?)','班级课表查询".toRegex().find(response)
			if(matcher != null){
				matcher.groups[1]?.value?.let{ gnmkdm = it }
				matcher.groups[2]?.value?.let{ url = it }
			}

			easAccount.getNoCheck("jwglxt/$url?gnmkdm=$gnmkdm&layout=default").use { response ->
				val html: String = response.body!!.string()

				// 从HTML里获取校区ID，专业号ID，班级ID
				val xqh_id = CodeUtils.matcher(XQHID_MATCHER, html)?.let{
					CodeUtils.matcher(OPTION_MATCHER, it) }
					?: throw CustomException("获取查询参数失败")
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
			Logger.e(e)
			result.error = "获取课表失败"
		}
		return result
	}


	/**
	 * 匹配学年信息
	 */
	private val TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})\\)")

	/**
	 * 获取学年信息
	 */
	private suspend fun getSchoolYearData(account: EASAccount, result: LessonTableQueryResult = LessonTableQueryResult()): LessonTableQueryResult {
		try {
			// 从教务获取本学年信息
			val response = account.getNoCheck(QustApi.EA_YEAR_DATA).use { it.body!!.string() }

			// 学年信息
			val matcher = TIME_MATCHER.matcher(response)
			if(matcher.find()) {
				val startDay = try { DateUtils.YMD.parse(matcher.group(3)!!)!! } catch(_: Exception) { Date() }
				val endDay = try { DateUtils.YMD.parse(matcher.group(4)!!)!! } catch(_: Exception){ Date() }
				result.termText = matcher.group()
				result.lessonTable.startDay = startDay
				result.lessonTable.totalWeek = DateUtils.calcWeekOffset(startDay, endDay).coerceAtLeast(1)
			}

			val doc: Document = Jsoup.parse(response)

			// 从thead里找出第1周是第几列
			var count = 0
			doc.getElementsByClass("tab-th-2").first().children().find {
				if(it.text() == "1") return@find true
				count++
				return@find false
			}

			// 从tbody的第一个tr标签（就是第一行）找到第n个td，其id就是需要的日期
			var find = 0
			doc.getElementsByTag("tbody").first().child(0).children().find{
				if(it.tagName() == "td"){
					if(find == count){
						result.lessonTable.startDay = DateUtils.YMD.parse(it.attr("id"))!!
						return@find true
					}
					find++
				}
				return@find false
			}
		} catch(e: Exception) {
			Logger.e(e)
		}
		return result
	}


	class CustomException(msg: String): RuntimeException(msg)
}
