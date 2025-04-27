package com.qust.helper.model.database

import com.qust.helper.entity.eas.Mark
import com.qust.helper.room.AppDataBase
import com.qust.helper.room.entity.MarkDao
import com.qust.helper.room.entity.toMark
import com.qust.helper.room.entity.toMarkDao

actual fun getMarkStorage(): MarkStorage = MarkStorageImpl()

class MarkStorageImpl: MarkStorage {

	override fun getMarksByKchId(id: String): List<Mark> {
		return AppDataBase.INSTANCE.markDao().selectByKchId(id).map(MarkDao::toMark)
	}

	override fun getMarksByIndex(index: Int): List<Mark> {
		return AppDataBase.INSTANCE.markDao().selectByIndex(index).map(MarkDao::toMark)
	}

	override fun updateAll(marks: List<Mark>){
		return AppDataBase.INSTANCE.markDao().updateAll(marks.map(Mark::toMarkDao))
	}

	override fun insertAll(marks: List<Mark>){
		return AppDataBase.INSTANCE.markDao().insertAll(marks.map(Mark::toMarkDao))
	}

	override fun setRead(id: Int) {
		return AppDataBase.INSTANCE.markDao().setRead(id)
	}

	override fun clear(index: Int) {
		return AppDataBase.INSTANCE.markDao().clear(index)
	}

}