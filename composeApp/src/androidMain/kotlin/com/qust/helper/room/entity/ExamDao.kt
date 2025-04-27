package com.qust.helper.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qust.helper.entity.eas.Exam

@Entity(tableName = "exams")
data class ExamDao(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,

	@ColumnInfo val term: Int = 0,

	@ColumnInfo val name: String = "",
	@ColumnInfo val place: String = "",
	@ColumnInfo val time: String = "",

	@ColumnInfo val isNew: Int,
)

fun Exam.toExamDao() = ExamDao(
	id = this.id,
	term = this.term,
	name = this.name,
	place = this.place,
	time = this.time,
	isNew = this.isNew,
)

fun ExamDao.toExam() = Exam(
	id = this.id,
	term = this.term,
	name = this.name,
	place = this.place,
	time = this.time,
	isNew = this.isNew,
)