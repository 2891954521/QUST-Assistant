package com.qust.helper.next.repository

import com.qust.helper.next.entity.eas.AcademicGroup
import com.qust.helper.next.entity.eas.AcademicInfo
import com.qust.helper.next.module.database.AppDataBase
import com.qust.helper.next.module.database.dao.AcademicGroupDao
import com.qust.helper.next.module.database.dao.AcademicInfoDao
import com.qust.helper.next.module.database.dao.toAcademicGroup
import com.qust.helper.next.module.database.dao.toAcademicGroupDao
import com.qust.helper.next.module.database.dao.toAcademicInfo
import com.qust.helper.next.module.database.dao.toAcademicInfoDao

object AcademicRepository {

	suspend fun getInfo(): List<AcademicInfo> {
		return AppDataBase.INSTANCE.academicDao().selectInfo().map(AcademicInfoDao::toAcademicInfo)
	}

	suspend fun getInfoByTerm(term: Int): List<AcademicInfo> {
		return AppDataBase.INSTANCE.academicDao().selectByTerm(term).map(AcademicInfoDao::toAcademicInfo)
	}

	suspend fun getInfoByGroup(group: Int): List<AcademicInfo> {
		return AppDataBase.INSTANCE.academicDao().selectByGroup(group).map(AcademicInfoDao::toAcademicInfo)
	}

	suspend fun getGroups(): List<AcademicGroup> {
		return AppDataBase.INSTANCE.academicDao().selectGroups().map(AcademicGroupDao::toAcademicGroup)
	}

	suspend fun getGroupsByTerm(): List<AcademicGroup> {
		return AppDataBase.INSTANCE.academicDao().selectGroupByTerm().map(AcademicGroupDao::toAcademicGroup)
	}

	suspend fun insertInfo(info: List<AcademicInfo>) {
		AppDataBase.INSTANCE.academicDao().insertInfo(info.map(AcademicInfo::toAcademicInfoDao))
	}

	suspend fun insertGroups(groups: List<AcademicGroup>) {
		AppDataBase.INSTANCE.academicDao().insertGroups(groups.map(AcademicGroup::toAcademicGroupDao))
	}

	suspend fun clearInfo() {
		AppDataBase.INSTANCE.academicDao().clearInfo()
	}

	suspend fun clearGroups() {
		AppDataBase.INSTANCE.academicDao().clearGroups()
	}
}
