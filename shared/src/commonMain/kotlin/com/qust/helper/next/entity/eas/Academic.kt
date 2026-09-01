package com.qust.helper.next.entity.eas

import androidx.compose.runtime.Immutable
import com.qust.helper.next.common.json.get
import kotlinx.serialization.json.JsonObject

/**
 * 学业情况查询课程
 *
 * @param kchId 课程号
 * @param name 课程名称
 * @param type 课程类型
 * @param status 修读状态
 * @param credit 学分
 * @param mark 成绩
 * @param gpa 绩点
 * @param category 课程类别名称
 * @param content 课程组成
 * @param index 学年学期索引
 * @param group 课程分组
 */
@Immutable
data class AcademicInfo(
	val id: Int = 0,

	val kchId: String = "",
	val name: String = "",
	val type: String = "",

	val credit: Float = 0F,
	val mark: String = "",
	val gpa: Float = 0F,

	val status: Int = 0,

	val category: String = "",
	val content: String = "",

	val index: Int = 0,
	val group: Int = 0,
) {
	companion object {
		fun createFromJson(js: JsonObject, entranceTime: Int, group: Int): AcademicInfo {
			val year: Int = if(js.containsKey("XNM")) {
				js["XNM", ""].toIntOrNull() ?: 0
			} else {
				js["JYXDXNM", ""].toIntOrNull() ?: 0
			}

			val term = if(js.containsKey("XQMMC")) {
				js["XQMMC", ""].toIntOrNull() ?: 0
			} else {
				js["JYXDXQMC", ""].toIntOrNull() ?: 0
			}

			return AcademicInfo(
				kchId = js["KCH_ID", ""],
				name = js["KCMC", ""].trim(),
				type = js["KCXZMC", ""],

				credit = js["XF", ""].toFloatOrNull() ?: 0F,
				mark = js["MAXCJ", ""],
				gpa = js["JD", 0F],

				status = js["XDZT", ""].toIntOrNull() ?: 0,

				category = js["KCLBMC", ""],
				content = js["XSXXXX", ""],

				index = (year - entranceTime) * 2 + term - 1,
				group = group,
			)
		}
	}
}

/**
 * @param group 课程分组
 * @param type 课程性质名称
 * @param requireCredits 要求学分
 * @param obtainedCredits 已获得学分
 * @param creditNotEarned 未通过学分
 * @param passedCounts 已通过门数
 * @param totalCounts 总共门数
 */
@Immutable
data class AcademicGroup(
	val group: Int = 0,
	val type: String = "",
	val requireCredits: Float = 0f,
	val obtainedCredits: Float = 0f,
	val creditNotEarned: Float = 0f,
	val passedCounts: Int = 0,
	val totalCounts: Int = 0,
) {
	class Builder(
		var group: Int = 0,
		var type: String = "",
		var requireCredits: Float = 0f,
		var obtainedCredits: Float = 0f,
		var creditNotEarned: Float = 0f,
		var passedCounts: Int = 0,
		var totalCounts: Int = 0,
	) {
		fun build() = AcademicGroup(
			group = group,
			type = type,
			requireCredits = requireCredits,
			obtainedCredits = obtainedCredits,
			creditNotEarned = creditNotEarned,
			passedCounts = passedCounts,
			totalCounts = totalCounts,
		)
	}
}
