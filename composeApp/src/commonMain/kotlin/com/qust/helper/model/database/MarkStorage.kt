package com.qust.helper.model.database

import com.qust.helper.entity.eas.Mark

expect fun getMarkStorage(): MarkStorage

interface MarkStorage {

	fun getMarksByKchId(id: String): List<Mark> = emptyList()

	fun getMarksByIndex(index: Int): List<Mark> = emptyList()

	fun updateAll(marks: List<Mark>){ }

	fun insertAll(marks: List<Mark>){ }

	fun setRead(id: Int){ }

	fun clear(index: Int){ }
}