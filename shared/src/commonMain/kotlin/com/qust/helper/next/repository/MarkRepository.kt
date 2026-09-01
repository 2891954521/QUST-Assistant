package com.qust.helper.next.repository

import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.module.database.AppDataBase
import com.qust.helper.next.module.database.dao.MarkDao
import com.qust.helper.next.module.database.dao.toMark
import com.qust.helper.next.module.database.dao.toMarkDao

object MarkRepository {

	suspend fun getMarksByTerm(term: Int): List<Mark> {
		return AppDataBase.INSTANCE.markDao().selectByTerm(term).map(MarkDao::toMark)
	}

	suspend fun setRead(id: Int) {
		AppDataBase.INSTANCE.markDao().setRead(id)
	}

	suspend fun insertAll(marks: List<Mark>) {
		AppDataBase.INSTANCE.markDao().insertAll(marks.map(Mark::toMarkDao))
	}

	suspend fun updateAll(marks: List<Mark>) {
		AppDataBase.INSTANCE.markDao().updateAll(marks.map(Mark::toMarkDao))
	}

	suspend fun clear(term: Int) {
		AppDataBase.INSTANCE.markDao().clear(term)
	}
}
