package com.qust.helper.model.database

import com.qust.helper.entity.eas.Exam
import com.qust.helper.room.AppDataBase
import com.qust.helper.room.entity.ExamDao
import com.qust.helper.room.entity.toExam
import com.qust.helper.room.entity.toExamDao

actual fun getExamStorage(): ExamStorage = ExamStorageImpl()

class ExamStorageImpl: ExamStorage {

	override fun getExamsByTerm(term: Int): List<Exam> {
		return AppDataBase.INSTANCE.examDao().selectByTerm(term).map(ExamDao::toExam)
	}

	override fun setRead(id: Int){
		return AppDataBase.INSTANCE.examDao().setRead(id)
	}

	override fun insertAll(exams: List<Exam>){
		AppDataBase.INSTANCE.examDao().insertAll(exams.map(Exam::toExamDao))
	}

	override fun updateAll(exams: List<Exam>){
		AppDataBase.INSTANCE.examDao().updateAll(exams.map(Exam::toExamDao))
	}
}