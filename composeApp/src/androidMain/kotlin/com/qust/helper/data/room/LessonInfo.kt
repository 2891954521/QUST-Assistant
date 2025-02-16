package com.qust.helper.data.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import org.json.JSONObject


/**
 * 学业情况查询课程
 *
 * @param kchId 课程号
 * @param name 课程名称
 * @param type 课程类型
 *
 * @param status 修读状态
 *
 * @param credit 学分
 * @param mark 成绩
 * @param gpa 绩点
 *
 * @param category 课程类别名称
 * @param content 课程组成
 *
 * @param index 学年学期索引
 */
@Entity(tableName = "lesson_info")
data class LessonInfo(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,

	@ColumnInfo val kchId: String,
	@ColumnInfo val name: String,
	@ColumnInfo val type: String,

	@ColumnInfo val credit: Float,
	@ColumnInfo val mark: String,
	@ColumnInfo val gpa: Float,

	@ColumnInfo val status: Int = 0,

	@ColumnInfo val category: String,
	@ColumnInfo val content: String,

	@ColumnInfo val index: Int,
	@ColumnInfo val group: Int,
){
	companion object {
		fun createFromJson(js: JSONObject, entranceTime: Int, group: Int): LessonInfo {
			val year = if(js.has("XNM")) { js.optString("XNM").let { if(it.isNotEmpty()) it.toInt() else 0 }
					} else { js.optString("JYXDXNM").let { if(it.isNotEmpty()) it.toInt() else 0 } }

			val term = if(js.has("XQMMC")) { js.optString("XQMMC").let { if(it.isNotEmpty()) it.toInt() else 0 }
					} else { js.optString("JYXDXQMC").let { if(it.isNotEmpty()) it.toInt() else 0 } }

			return LessonInfo(
				kchId = js.optString("KCH_ID"),
				name = js.optString("KCMC").trim(),
				type = js.optString("KCXZMC"),

				credit = js.optString("XF").toFloatOrNull() ?: 0F,
				mark = js.optString("MAXCJ"),
				gpa = js.optDouble("JD").let{ if(it.isNaN()) 0F else it.toFloat() },

				status = js.optString("XDZT").toIntOrNull() ?: 0,

				category = js.optString("KCLBMC"),
				content = js.optString("XSXXXX"),

				index = (year - entranceTime) * 2 + term - 1,
				group = group
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
@Entity(tableName = "lesson_info_group")
data class LessonInfoGroup(
	@PrimaryKey val group: Int,
	val type: String = "",
	val requireCredits: Float = 0f,
	val obtainedCredits: Float = 0f,
	val creditNotEarned: Float = 0f,
	val passedCounts: Int = 0,
	val totalCounts: Int = 0
){
	class Builder(
		var group: Int = 0,
		var type: String = "",
		var requireCredits: Float = 0f,
		var obtainedCredits: Float = 0f,
		var creditNotEarned: Float = 0f,
		var passedCounts: Int = 0,
		var totalCounts: Int = 0,
	) {
		fun build(): LessonInfoGroup {
			return LessonInfoGroup(group, type, requireCredits, obtainedCredits, creditNotEarned, passedCounts, totalCounts)
		}
	}
}

@Dao
interface LessonInfoDao {

	@Query("SELECT * FROM lesson_info")
	fun selectAll(): List<LessonInfo>

	@Query("SELECT * FROM lesson_info WHERE `index` = :index")
	fun selectByIndex(index: Int): List<LessonInfo>

	@Query("SELECT * FROM lesson_info WHERE `group` = :group")
	fun selectByGroup(group: Int): List<LessonInfo>

	@Query("SELECT `index` AS 'group', " +
			"'' AS type, " +
			"COUNT(1) AS totalCounts, " +
			"SUM(credit) AS requireCredits, " +
			"SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS passedCounts, " +
			"SUM(CASE WHEN status = 4 THEN credit ELSE 0 END) AS obtainedCredits, " +
			"SUM(CASE WHEN status != 4 THEN credit ELSE 0 END) AS creditNotEarned " +
			"FROM lesson_info GROUP BY `index`")
	fun selectIndex(): List<LessonInfoGroup>

	@Query("SELECT * FROM lesson_info_group")
	fun selectGroups(): List<LessonInfoGroup>

	@Insert
	fun insert(lesson: LessonInfo)

	@Insert
	fun insertAll(lessons: List<LessonInfo>)

	@Insert
	fun insertAllGroups(lessons: List<LessonInfoGroup>)

	@Query("DELETE FROM lesson_info")
	fun clear()

	@Query("DELETE FROM lesson_info_group")
	fun clearGroups()
}

