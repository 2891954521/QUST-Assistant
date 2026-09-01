package com.qust.helper.next.module.database.dao

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.qust.helper.next.common.json.decodeToList
import com.qust.helper.next.common.json.encodeToJson
import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.utils.DateUtils.epochMillisecondsToDateTime
import com.qust.helper.next.utils.DateUtils.toInstant

@Entity(tableName = "marks")
class MarkDao(

	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,

	@ColumnInfo val kchId: String = "",
	@ColumnInfo val name: String = "",
	@ColumnInfo val type: String = "",

	@ColumnInfo val credit: String = "",
	@ColumnInfo val gpa: String = "",
	@ColumnInfo val mark: Float = 0F,

	@ColumnInfo val items: String = "[]",

	@ColumnInfo val term: Int = 0,
	@ColumnInfo val time: Long = 0L,
	@ColumnInfo val isNew: Int = 1,
)

fun MarkDao.toMark(): Mark = Mark(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	gpa = this.gpa,
	mark = this.mark,
	items = if(this.items.isBlank() || this.items == "[]") emptyList() else this.items.decodeToList(),
	term = this.term,
	time = this.time.epochMillisecondsToDateTime(),
	isNew = this.isNew,
)

fun Mark.toMarkDao(): MarkDao = MarkDao(
	id = this.id,
	kchId = this.kchId,
	name = this.name,
	type = this.type,
	credit = this.credit,
	gpa = this.gpa,
	mark = this.mark,
	items = if(this.items.isEmpty()) "[]" else this.items.encodeToJson(),
	term = this.term,
	time = this.time.toInstant().toEpochMilliseconds(),
	isNew = this.isNew,
)
