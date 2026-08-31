package com.qust.helper.next.module.database.dao

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.qust.helper.next.entity.eas.Exam

@Entity(tableName = "exams")
class ExamDao(

	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,

	@ColumnInfo val term: Int = 0,

	@ColumnInfo val name: String = "",
	@ColumnInfo val place: String = "",
	@ColumnInfo val time: String = "",

	@ColumnInfo val isNew: Int = 1,
)

fun ExamDao.toExam(): Exam = Exam(
	id = this.id,
	term = this.term,
	name = this.name,
	place = this.place,
	time = this.time,
	isNew = this.isNew,
)

fun Exam.toExamDao(): ExamDao = ExamDao(
	id = this.id,
	term = this.term,
	name = this.name,
	place = this.place,
	time = this.time,
	isNew = this.isNew,
)
