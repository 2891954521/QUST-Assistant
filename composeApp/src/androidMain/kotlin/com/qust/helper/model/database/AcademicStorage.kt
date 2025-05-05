package com.qust.helper.model.database

import com.qust.helper.entity.eas.AcademicGroup
import com.qust.helper.entity.eas.AcademicInfo
import com.qust.helper.room.AppDataBase
import com.qust.helper.room.entity.AcademicGroupDao
import com.qust.helper.room.entity.AcademicInfoDao
import com.qust.helper.room.entity.toAcademicGroup
import com.qust.helper.room.entity.toAcademicGroupDao
import com.qust.helper.room.entity.toAcademicInfo
import com.qust.helper.room.entity.toAcademicInfoDao

actual fun getAcademicStorage(): AcademicStorage = AcademicStorageImpl()

class AcademicStorageImpl: AcademicStorage {

	override suspend fun getInfo(): List<AcademicInfo> {
		return  AppDataBase.INSTANCE.academicDao().selectInfo().map(AcademicInfoDao::toAcademicInfo)
	}

	override suspend fun getInfoByTerm(term: Int): List<AcademicInfo> {
		return AppDataBase.INSTANCE.academicDao().selectByTerm(term).map(AcademicInfoDao::toAcademicInfo)
	}

	override suspend fun getInfoByGroup(group: Int): List<AcademicInfo> {
		return AppDataBase.INSTANCE.academicDao().selectByGroup(group).map(AcademicInfoDao::toAcademicInfo)
	}

	override suspend fun getGroups(): List<AcademicGroup> {
		return AppDataBase.INSTANCE.academicDao().selectGroups().map(AcademicGroupDao::toAcademicGroup)
	}

	override suspend fun getGroupsByTerm(): List<AcademicGroup> {
		return AppDataBase.INSTANCE.academicDao().selectGroupByTerm().map(AcademicGroupDao::toAcademicGroup)
	}

	override suspend fun insertInfo(info: List<AcademicInfo>) {
		AppDataBase.INSTANCE.academicDao().insertInfo(info.map(AcademicInfo::toAcademicInfoDao))
	}

	override suspend fun insertGroups(groups: List<AcademicGroup>) {
		AppDataBase.INSTANCE.academicDao().insertGroups(groups.map(AcademicGroup::toAcademicGroupDao))
	}

	override suspend fun clearInfo() {
		AppDataBase.INSTANCE.academicDao().clearInfo()
	}

	override suspend fun clearGroups() {
		AppDataBase.INSTANCE.academicDao().clearGroups()
	}
}