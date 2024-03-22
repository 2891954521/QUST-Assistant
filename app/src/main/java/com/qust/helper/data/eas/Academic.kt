package com.qust.helper.data.eas

import kotlinx.serialization.Serializable
import org.json.JSONObject


object Academic{

	/**
	 * 学业情况查询课程
	 * @param status 修读状态
	 * @param name 课程名称
	 * @param type 课程类型
	 * @param category 课程类别名称
	 * @param content 课程组成
	 * @param year 学年
	 * @param term 学期
	 * @param credit 学分
	 * @param mark 成绩
	 * @param gpa 绩点
	 */
	@Serializable
	data class LessonInfo(
		var status: Int = 0,
		var name: String = "",
		var type: String = "",
		var category: String = "",
		var content: String = "",
		var year: Int = 0,
		var term: Int = 0,
		var credit: String = "",
		var mark: String = "",
		var gpa: Float = 0f
	) {
		companion object {
			fun createFromJson(js: JSONObject): LessonInfo {
				return LessonInfo(
					name = if(js.has("KCMC")) js.getString("KCMC").trim() else "",
					type = if(js.has("KCXZMC")) js.getString("KCXZMC") else "",
					category = if(js.has("KCLBMC")) js.getString("KCLBMC") else "",
					content = if(js.has("XSXXXX")) js.getString("XSXXXX") else "",
					year = if(js.has("XNM") || js.has("JYXDXNM")) js.getString(if(js.has("XNM")) "XNM" else "JYXDXNM").toInt() else 0,
					term = if(js.has("XQMMC") || js.has("JYXDXQMC")) js.getString(if(js.has("XQMMC")) "XQMMC" else "JYXDXQMC").toInt() else 0,
					status = if(js.has("XDZT")) js.getString("XDZT").toInt() else 0,
					credit = if(js.has("XF")) js.getString("XF") else "",
					mark = if(js.has("MAXCJ")) js.getString("MAXCJ") else "",
					gpa = if(js.has("JD")) js.getDouble("JD").toFloat() else 0F
				)
			}
		}
	}

	/**
	 * @param requireCredits 要求学分
	 * @param obtainedCredits 已获得学分
	 * @param creditNotEarned 未通过学分
	 * @param passedCounts 已通过门数
	 * @param groupName 分组名称
	 * @param lessonIndex 课程索引
	 */
	@Serializable
	data class LessonInfoGroup private constructor(
		var requireCredits: Float = 0f,
		var obtainedCredits: Float = 0f,
		var creditNotEarned: Float = 0f,
		var passedCounts: Int = 0,
		var groupName: String = "",
		var lessonIndex: IntArray = IntArray(0)
	) {

		class Builder(groupName: String = "") {
			private val group: LessonInfoGroup = LessonInfoGroup(groupName = groupName)

			private var lessonIndex: ArrayList<Int> = ArrayList(32)

			var requireCredits: Float = 0F
				set(value) {
					field = value
					group.requireCredits = field
				}

			var obtainedCredits: Float = 0F
				set(value) {
					field = value
					group.obtainedCredits = field
				}

			var creditNotEarned: Float = 0F
				set(value) {
					field = value
					group.creditNotEarned = field
				}

			var passedCounts: Int = 0
				set(value) {
					field = value
					group.passedCounts = field
				}

			var groupName: String = ""
				set(value){
					field = value
					group.groupName = field
				}

			/**
			 * 添加一门科目
			 */
			fun addLesson(index: Int) {
				lessonIndex.add(index)
			}

			fun hasLesson(): Boolean{
				return lessonIndex.isNotEmpty()
			}

			fun build(): LessonInfoGroup {
				group.lessonIndex = lessonIndex.toIntArray()
				return group
			}
		}

		override fun equals(other: Any?): Boolean {
			if(this === other) return true
			if(javaClass != other?.javaClass) return false

			other as LessonInfoGroup

			if(requireCredits != other.requireCredits) return false
			if(obtainedCredits != other.obtainedCredits) return false
			if(creditNotEarned != other.creditNotEarned) return false
			if(passedCounts != other.passedCounts) return false
			if(groupName != other.groupName) return false
			return lessonIndex.contentEquals(other.lessonIndex)
		}

		override fun hashCode(): Int {
			var result = requireCredits.hashCode()
			result = 31 * result + obtainedCredits.hashCode()
			result = 31 * result + creditNotEarned.hashCode()
			result = 31 * result + passedCounts
			result = 31 * result + groupName.hashCode()
			result = 31 * result + lessonIndex.contentHashCode()
			return result
		}
	}

	val LESSON_TYPE = arrayOf(
		"", "在修", "未过", "未修", "已修",
		"校内被替代课程",
		"校内课程替代",
		"校内课程替代节点",
		"校外课程替换节点/校外认定课程",
		"校内被认定课程",
		"学业预警不审核课程"
	)

}