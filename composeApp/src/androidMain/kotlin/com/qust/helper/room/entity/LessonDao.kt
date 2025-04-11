package com.qust.helper.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qust.helper.entity.lesson.Lesson

@Entity(tableName = "lesson")
class LessonDao(

	@PrimaryKey(autoGenerate = true)
	val id: Long = 0L,

	@ColumnInfo val type: Int = 0,
	@ColumnInfo val reference: Long = 0L,

	@ColumnInfo val lessonId: String = "",

	@ColumnInfo val colorLabel: Int = 0,

	@ColumnInfo val weeks: Long = 0L,
	@ColumnInfo val week: Int = 0,
	@ColumnInfo val startMinute: Int = 0,
	@ColumnInfo val endMinute: Int = 0,

	@ColumnInfo val name: String = "",
	@ColumnInfo val place: String = "",
	@ColumnInfo val teacher: String = "",

	@ColumnInfo val remark: String = ""
)

fun LessonDao.toLesson(): Lesson = Lesson(
	id = this.id,
	type = this.type,
	reference = this.reference,
	lessonId = this.lessonId,
	colorLabel = this.colorLabel,
	weeks = this.weeks,
	week = this.week,
	startMinute = this.startMinute,
	endMinute = this.endMinute,
	name = this.name,
	place = this.place,
	teacher = this.teacher,
	remark = this.remark,
)

fun Lesson.toLessonDao(): LessonDao = LessonDao(
	id = this.id,
	type = this.type,
	reference = this.reference,
	lessonId = this.lessonId,
	colorLabel = this.colorLabel,
	weeks = this.weeks,
	week = this.week,
	startMinute = this.startMinute,
	endMinute = this.endMinute,
	name = this.name,
	place = this.place,
	teacher = this.teacher,
	remark = this.remark,
)

