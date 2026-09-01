package com.qust.helper.next.module.eas

import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.entity.eas.AcademicGroup
import com.qust.helper.next.entity.eas.AcademicInfo
import com.qust.helper.next.network.api.QustApi
import com.qust.helper.next.network.client.EasHttpClient
import com.qust.helper.next.repository.AccountRepository
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.parameters
import io.ktor.http.takeFrom
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject

object AcademicModel {

	/**
	 * 匹配课程类别
	 */
	private val xfyqjdId = Regex(" xfyqjd_id='(.*?)'")

	/**
	 * 匹配要求学分
	 */
	private val xfyqjdIdYxxf = Regex(" xfyqjd_id='([a-zA-Z\\d]+)'.*?yxxf='([\\d.]+)' yqzdxf='([\\d.]+)'")

	/**
	 * 查询学业情况
	 */
	suspend fun getAcademic(): Pair<List<AcademicGroup>, List<AcademicInfo>> {
		val academicInfo = mutableListOf<AcademicInfo>()
		val lessonGroups = LinkedHashMap<String, AcademicGroup.Builder>()

		try {
			val html = getAcademicPageHtml()

			xfyqjdId.findAll(html).forEach { match ->
				val id = match.groupValues[1]
				if(!lessonGroups.containsKey(id)) {
					lessonGroups[id] = AcademicGroup.Builder()
				}
			}

			xfyqjdIdYxxf.findAll(html).forEach { match ->
				val id = match.groupValues[1]
				val group = lessonGroups[id] ?: return@forEach
				group.obtainedCredits = match.groupValues[2].toFloatOrNull() ?: 0F
				group.requireCredits = match.groupValues[3].toFloatOrNull() ?: 0F
			}

			val entranceTime = AccountRepository.entranceDate
			val accountName = AppSetting[DataKeys.EAS_ACCOUNT, ""]

			lessonGroups.keys.forEachIndexed { index, key ->
				val array = EasHttpClient.post<FormDataContent, JsonArray>(
					url = QustApi.ACADEMIC_INFO,
					body = FormDataContent(parameters {
						append("xfyqjd_id", key)
						append("xh_id", accountName)
					})
				)

				if(array.isEmpty()) return@forEachIndexed

				val group = lessonGroups[key]!!
				group.group = index
				for(i in array.indices) {
					val info = AcademicInfo.createFromJson(array[i].jsonObject, entranceTime, index)
					when(info.status) {
						2 -> group.creditNotEarned += info.credit
						4 -> group.passedCounts++
					}
					group.totalCounts++
					academicInfo.add(info)
				}
				group.type = academicInfo.last().type
			}

			return Pair(
				lessonGroups.values.filter { it.totalCounts > 0 }.map { it.build() },
				academicInfo,
			)
		} catch(e: Exception) {
			Logger.e(e = e)
		}

		return Pair(emptyList(), emptyList())
	}

	private suspend fun getAcademicPageHtml(): String {
		val builder = HttpRequestBuilder().apply {
			method = HttpMethod.Get
			url.takeFrom(EasHttpClient.baseHttpUrl)
			url.appendPathSegments("jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html")
			parameter("gnmkdm", "N105515")
			parameter("layout", "default")
		}
		return EasHttpClient.executeAsString(builder)
	}
}
