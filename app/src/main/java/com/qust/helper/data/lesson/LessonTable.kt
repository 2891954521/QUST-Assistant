package com.qust.helper.data.lesson

import com.qust.helper.model.Logger
import com.qust.helper.ui.theme.BACKGROUND_COLORS
import com.qust.helper.utils.DateUtils
import kotlinx.serialization.Serializable
import org.json.JSONObject

import java.text.ParseException
import java.util.Date

/**
 * 课表
 * @param startDay 开学时间
 * @param totalWeek 总周数
 */
@Serializable
data class LessonTable(
	@Serializable(with = DateUtils.DateSerializer::class)
	var startDay: Date = Date(),
	var totalWeek: Int = 1,
	var lessons: Array<Array<LessonGroup?>> = Array(7) {
		arrayOfNulls(10)
	},
){

	constructor(_startDay: String, _totalWeek: Int) : this(totalWeek = _totalWeek) {
		try{
			setStartDay(_startDay)
		}catch(e: ParseException){
			startDay = Date()
		}
	}

	fun setStartDay(startDay: String) {
		this.startDay = DateUtils.YMD.parse(startDay)!!
	}

	fun getLessonGroupNotNull(week: Int, count: Int): LessonGroup {
		if(lessons[week][count] == null) {
			lessons[week][count] = LessonGroup(week + 1, count + 1)
		}
		return lessons[week][count]!!
	}

	/**
	 * 从json中解析课表
	 */
	fun loadFromJson(json: JSONObject): Boolean {
		return try {
			if(!json.has("kbList")) return false

			var index = 0
			val colors = ArrayList<String>()
			val array = json.getJSONArray("kbList")

			for(i in 0 until array.length()) {
				val js = array.getJSONObject(i)
				val week = js.getInt("xqj")
				val sp = js.getString("jcs").split("-")
				val count = sp[0].toInt()
				val lesson = Lesson.loadFromJson(js)
				lesson.len = sp[1].toInt() - count + 1

				val j = colors.indexOf(lesson.name)
				if(j != -1){
					lesson.color = j % (BACKGROUND_COLORS.size - 1) + 1
				}else{
					if(++index == BACKGROUND_COLORS.size) index = 1
					lesson.color = index
					colors.add(lesson.name)
				}

				if(lessons[week - 1][count - 1] == null) {
					lessons[week - 1][count - 1] = LessonGroup(week, count)
				}
				lessons[week - 1][count - 1]!!.addLesson(lesson)
			}
			true
		} catch(e: Exception) {
			Logger.e(json.toString(), e)
			false
		}
	}

	/**
	 * 将用户定义的课程合并到当前课程
	 * @param other
	 */
	fun mergeUserDefinedLesson(other: LessonTable): LessonTable {
		for(i in lessons.indices) {
			val groups = lessons[i]
			val otherGroups = other.lessons[i]
			for(j in groups.indices) {
				otherGroups[j]?.let { otherGroup ->
					if(groups[j] == null) groups[j] = LessonGroup(i + 1, j + 1)
					groups[j]!!.mergeUserDefinedLesson(otherGroup)
				}
			}
		}
		return this
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as LessonTable

		if(startDay != other.startDay) return false
		if(totalWeek != other.totalWeek) return false
		return lessons.contentDeepEquals(other.lessons)
	}

	override fun hashCode(): Int {
		var result = startDay.hashCode()
		result = 31 * result + totalWeek
		result = 31 * result + lessons.contentDeepHashCode()
		return result
	}
}
