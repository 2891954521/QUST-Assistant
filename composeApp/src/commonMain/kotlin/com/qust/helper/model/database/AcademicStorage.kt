package com.qust.helper.model.database

import com.qust.helper.entity.eas.AcademicGroup
import com.qust.helper.entity.eas.AcademicInfo


expect fun getAcademicStorage(): AcademicStorage

interface AcademicStorage {

	suspend fun getInfo(): List<AcademicInfo> = emptyList()

	/**
	 * 获取建议修读年份为指定值的课程
	 */
	suspend fun getInfoByTerm(term: Int): List<AcademicInfo> = emptyList()

	/**
	 * 获取课程类型为指定值的课程
	 */
	suspend fun getInfoByGroup(group: Int): List<AcademicInfo> = emptyList()


	suspend fun getGroups(): List<AcademicGroup> = emptyList()

	/**
	 * 获取按照建议修读年份排序的课程组
	 */
	suspend fun getGroupsByTerm(): List<AcademicGroup> = emptyList()


	suspend fun insertInfo(info: List<AcademicInfo>){ }

	suspend fun insertGroups(groups: List<AcademicGroup>){ }

	suspend fun clearInfo(){ }

	suspend fun clearGroups(){ }
}
