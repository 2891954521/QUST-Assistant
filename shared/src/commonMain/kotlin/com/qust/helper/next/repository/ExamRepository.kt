package com.qust.helper.next.repository

import com.qust.helper.next.entity.eas.Exam
import com.qust.helper.next.module.database.AppDataBase
import com.qust.helper.next.module.database.dao.ExamDao
import com.qust.helper.next.module.database.dao.toExam
import com.qust.helper.next.module.database.dao.toExamDao

object ExamRepository {

	suspend fun getExamsByTerm(term: Int): List<Exam> {
		return AppDataBase.INSTANCE.examDao().selectByTerm(term).map(ExamDao::toExam)
	}

	suspend fun setRead(id: Int) {
		AppDataBase.INSTANCE.examDao().setRead(id)
	}

	suspend fun insertAll(exams: List<Exam>) {
		AppDataBase.INSTANCE.examDao().insertAll(exams.map(Exam::toExamDao))
	}

	suspend fun updateAll(exams: List<Exam>) {
		AppDataBase.INSTANCE.examDao().updateAll(exams.map(Exam::toExamDao))
	}

	suspend fun clear(term: Int) {
		AppDataBase.INSTANCE.examDao().clear(term)
	}
}
