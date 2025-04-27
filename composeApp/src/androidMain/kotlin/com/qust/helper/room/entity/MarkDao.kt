package com.qust.helper.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.qust.helper.entity.eas.Mark
import com.qust.helper.utils.toInstant
import com.qust.helper.utils.toLocalDateTime
import kotlinx.datetime.Instant
import org.json.JSONArray

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
data class MarkDao(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,

	@ColumnInfo val kchId: String,
	@ColumnInfo val name: String,
	@ColumnInfo val type: String,

	@ColumnInfo val credit: String,
	@ColumnInfo val gpa: String,
	@ColumnInfo val mark: Float,

	@ColumnInfo val items: Array<Mark.Item> = emptyArray(),

	@ColumnInfo val index: Int,
	@ColumnInfo val time: Long,
	@ColumnInfo val isNew: Int,
){

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(other !is MarkDao) return false

		if(id != other.id) return false
		if(mark != other.mark) return false
		if(index != other.index) return false
		if(isNew != other.isNew) return false
		if(kchId != other.kchId) return false
		if(name != other.name) return false
		if(type != other.type) return false
		if(credit != other.credit) return false
		if(gpa != other.gpa) return false
		if(!items.contentEquals(other.items)) return false
		if(time != other.time) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id
		result = 31 * result + mark.hashCode()
		result = 31 * result + index
		result = 31 * result + isNew
		result = 31 * result + kchId.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + credit.hashCode()
		result = 31 * result + gpa.hashCode()
		result = 31 * result + items.contentHashCode()
		result = 31 * result + time.hashCode()
		return result
	}

}

fun MarkDao.toMark() = Mark(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	gpa = this.gpa,
	mark = this.mark,
	items = this.items,
	index = this.index,
	time = Instant.fromEpochMilliseconds(this.time).toLocalDateTime(),
	isNew = this.isNew,
)

fun Mark.toMarkDao() = MarkDao(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	gpa = this.gpa,
	mark = this.mark,
	items = this.items,
	index = this.index,
	time = this.time.toInstant().toEpochMilliseconds(),
	isNew = this.isNew,
)


class MarkItemConverter {

	@TypeConverter
	fun objectToString(list: Array<Mark.Item>?): String {
		if(list.isNullOrEmpty()) return "[]"
		val sb = StringBuilder("[")
		for(item in list){
			sb.append('"')
				.append(item.name)
				.append("\",\"")
				.append(item.mark)
				.append("\",")
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
}