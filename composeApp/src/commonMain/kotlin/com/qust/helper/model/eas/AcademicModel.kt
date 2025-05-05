package com.qust.helper.model.eas

import com.qust.helper.data.QustApi
import com.qust.helper.entity.eas.AcademicGroup
import com.qust.helper.entity.eas.AcademicInfo
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.database.AcademicStorage
import com.qust.helper.model.database.getAcademicStorage
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.Logger
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import java.util.regex.Pattern

object AcademicModel: AcademicStorage by getAcademicStorage() {
	
	/**
	 * 匹配课程类别
	 */
	private val xfyqjd_id = Pattern.compile(" xfyqjd_id='(.*?)'")
	
	/**
	 * 匹配要求学分
	 */
	private val xfyqjd_id_yxxf_yqzdxf = Pattern.compile(" xfyqjd_id='([a-zA-Z\\d]+)'.*?yxxf='([\\d.]+)' yqzdxf='([\\d.]+)'")
	
	/**
	 * 查询学业情况
	 */
	suspend fun getAcademic(account: EasAccount): Pair<List<AcademicGroup>, Array<AcademicInfo>> {
		// 查询到的所有课程
		val academicInfo = mutableListOf<AcademicInfo>()
		// 储存所有课程的分组
		val lessonGroups = LinkedHashMap<String, AcademicGroup.Builder>()

		try{
			var html = account.get<String>(QustApi.ACADEMIC_PAGE)

			var matcher = xfyqjd_id.matcher(html)
			while(matcher.find()){
				val id = matcher.group(1)!!
				if(!lessonGroups.containsKey(id)) lessonGroups[id] = AcademicGroup.Builder()
			}

			matcher = xfyqjd_id_yxxf_yqzdxf.matcher(html)
			while(matcher.find()){
				val id = matcher.group(1)!!
				if(lessonGroups.containsKey(id)){
					val group = lessonGroups[id]!!
					group.obtainedCredits = matcher.group(2)?.toFloatOrNull() ?: 0F
					group.requireCredits = matcher.group(3)?.toFloatOrNull() ?: 0F
				}
			}

			val entranceTime = account.entranceDate
			lessonGroups.keys.forEachIndexed { index, key ->
				html = account.post<String>(QustApi.ACADEMIC_INFO){
					setBody(FormDataContent(parameters {
						append("xfyqjd_id", key)
						append("xh_id", account.getAccountName())
					}))
				}
				val array = JsonUtils.parseString<JsonArray>(html)
				if(array.size > 0){
					val group = lessonGroups[key]!!
					group.group = index
					for(i in 0 ..< array.size) {
						val info = AcademicInfo.createFromJson(array[i].jsonObject, entranceTime, index)
						if(info.status == 2) {
							// 统计未过课程的学分
							group.creditNotEarned += info.credit
						}else if(info.status == 4){
							// 统计已修门数
							group.passedCounts++
						}
						group.totalCounts++
						academicInfo.add(info)
					}
					group.type = academicInfo[academicInfo.size - 1].type
				}
			}
			return Pair(lessonGroups.values.filter{ it.totalCounts > 0 }.map{ it.build() }, academicInfo.toTypedArray())
		}catch(e: Exception){
			Logger.e(e = e)
		}

		return Pair(emptyList(), emptyArray())
	}
}