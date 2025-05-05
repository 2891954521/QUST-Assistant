package com.qust.helper.model.database

import com.qust.helper.entity.eas.Exam

expect fun getExamStorage(): ExamStorage

interface ExamStorage {

	fun getExamsByTerm(term: Int): List<Exam> = emptyList()

	fun setRead(id: Int){ }

	fun insertAll(exams: List<Exam>){ }

	fun updateAll(exams: List<Exam>){ }
}
