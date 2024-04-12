package com.qust.helper.data.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.qust.helper.utils.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.Locale

/**
 *
 * @param kchId 课程号
 * @param name 课程名称
 * @param type 课程类型
 *
 * @param credit 学分
 * @param gpa 绩点
 * @param mark 成绩
 *
 * @param items 成绩条目
 *
 * @param index 学年学期索引
 * @param time 出成绩时间
 * @param isNew 是否是新查询到的
 */
@Entity(tableName = "marks")
@TypeConverters(MarkItemConverter::class)
data class Mark(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,

	@ColumnInfo(name = "kchId") val kchId: String,
	@ColumnInfo(name = "name") val name: String,
	@ColumnInfo(name = "type") val type: String,

	@ColumnInfo(name = "credit") val credit: String,
	@ColumnInfo(name = "gpa") val gpa: String,
	@ColumnInfo(name = "mark") val mark: Float,

	@ColumnInfo(name = "items") val items: Array<Item> = emptyArray(),

	@ColumnInfo(name = "index") val index: Int,
	@ColumnInfo(name = "time") val time: Date,
	@ColumnInfo(name = "isNew") val isNew: Int,
){
	companion object {
		/**
		 * 解析 js 为 mark 对象
		 */
		fun createFromJson(js: JSONObject): Builder {
			val date = try {
				if(js.has("tjsj")){
					DateUtils.YMD_HMS.parse(js.getString("tjsj"))
				} else Date()
			} catch(_: Exception){
				Date()
			}
			return Builder(
				kchId = js.getString("kch_id"),
				name = js.getString("kcmc").trim(),
				type = if(js.has("ksxz")) js.getString("ksxz") else "正常考试",
				credit = js.getString("xf"),
				mark = if(js.has("cj")) js.getString("cj").toFloatOrNull() ?: 0F else 0F,
				time = date
			)
		}
	}

	class Builder(
		val kchId: String = "",
		val name: String = "",
		val type: String = "",
		val credit: String = "",
		var mark: Float = 0F,
		val time: Date = Date()
	){
		var gpa: String =
			if(mark < 60) "0"
			else if("正常考试" == type) String.format(Locale.CHINA, "%.2f",mark / 10 - 5)
			else "1"

		val items: ArrayList<Item> = ArrayList(4)

		/**
		 * 添加一条成绩明细
		 */
		fun addItemMark(js: JSONObject) {
			if(!js.has("xmblmc")) return
			val itemName = js.getString("xmblmc")
			if("总评" == itemName){
				if(mark == 0F && js.has("xmcj")){
					mark = js.getString("xmcj").toFloatOrNull() ?: 0F
					gpa =
						if(mark < 60) "0"
						else if("正常考试" == type) String.format(Locale.CHINA, "%.2f",mark / 10 - 5)
						else "1"
				}
			}else{
				items.add(Item(itemName, if(js.has("xmcj")) js.getString("xmcj") else ""))
			}
		}

		fun build(index: Int = 0, new: Boolean = false): Mark{
			return Mark(
				kchId = kchId,
				name = name,
				type = type,
				credit = credit,
				gpa = gpa,
				mark = mark,
				items = items.toTypedArray(),
				index = index,
				time = time,
				isNew = if(new) 1 else 0
			)
		}
	}

	data class Item(val name: String, val mark: String)

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as Mark

		if(kchId != other.kchId) return false
		if(name != other.name) return false
		if(type != other.type) return false
		if(credit != other.credit) return false
		if(gpa != other.gpa) return false
		if(mark != other.mark) return false
		if(!items.contentEquals(other.items)) return false
		if(index != other.index) return false
		return time == other.time
	}

	override fun hashCode(): Int {
		var result = kchId.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + credit.hashCode()
		result = 31 * result + gpa.hashCode()
		result = 31 * result + mark.hashCode()
		result = 31 * result + items.contentHashCode()
		result = 31 * result + index
		result = 31 * result + time.hashCode()
		return result
	}
}

@Dao
interface MarkDao {

	@Query("SELECT * FROM marks WHERE `index` LIKE :index")
	fun selectByIndex(index: Int): List<Mark>

	@Query("UPDATE marks SET isNew = 0 WHERE id = :id")
	fun setRead(id: Int)

	@Insert
	fun insert(lesson: Mark)

	@Insert
	fun insertAll(lessons: List<Mark>)

	@Update
	fun update(lesson: Mark)


	@Delete
	fun delete(lesson: Mark)
}


class MarkItemConverter {

	@TypeConverter
	fun objectToString(list: Array<Mark.Item>?): String {
		if(list.isNullOrEmpty()) return "[]"
		val sb = StringBuilder("[")
		for(item in list){
			sb.append('"')
			sb.append(item.name)
			sb.append("\",\"")
			sb.append(item.mark)
			sb.append("\",")
		}
		sb.setLength(sb.length - 1)
		sb.append(']')
		return sb.toString()
	}

	@TypeConverter
	fun stringToObject(json: String?): Array<Mark.Item> {
		if(json == null) return emptyArray()
		val array = JSONArray(json)
		return Array(array.length() / 2){ Mark.Item(array.getString(it * 2), array.getString(it * 2 + 1)) }
	}

	@TypeConverter
	fun fromTimestamp(value: Long?): Date? {
		return if(value == null) null else Date(value)
	}

	@TypeConverter
	fun dateToTimestamp(date: Date?): Long? {
		return date?.time
	}
}