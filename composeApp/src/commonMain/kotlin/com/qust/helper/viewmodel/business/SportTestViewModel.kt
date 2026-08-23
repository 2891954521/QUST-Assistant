package com.qust.helper.viewmodel.business

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.business.SportScore
import com.qust.helper.entity.business.SportTask
import com.qust.helper.entity.business.SportUpload
import com.qust.helper.entity.business.SportYear
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.model.account.VpnAccount
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * 智慧体测查询 ViewModel
 */
class SportTestViewModel : RequestViewModel() {

	private val vpnAccount = VpnAccount("tiyu.qust.edu.cn", "https")

	var needLogin by mutableStateOf(false)

	var years = mutableStateOf<List<SportYear>>(emptyList())
	var tasks = mutableStateOf<List<SportTask>>(emptyList())
	var scores = mutableStateOf<List<SportUpload>>(emptyList())
	var detail = mutableStateOf<List<SportScore>>(emptyList())

	var selectedYear by mutableStateOf(0)
	var selectedTask by mutableStateOf(0)

	var loading by mutableStateOf(false)

	/**
	 * 查询学年列表
	 */
	fun queryYears() {
		request({
			val resp = vpnAccount.getOriginal("api/basic/school/year/list").bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			val result: JsonArray? = js["result", JSONArray]
			val list = result?.mapNotNull {
				val obj = it.jsonObject
				SportYear(
					text = obj["text"]?.jsonPrimitive?.contentOrNull ?: "",
					value = obj["value"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
				)
			} ?: emptyList()
			years.value = list
			if(list.isNotEmpty()) {
				selectedYear = list[0].value
				queryTasks(list[0].value)
			}
		}, {
			Logger.e(e = it)
			toastError("获取学年列表失败：${it.message}")
		})
	}

	/**
	 * 查询指定学年的任务列表
	 */
	fun queryTasks(year: Int) {
		selectedYear = year
		tasks.value = emptyList()
		request({
			val resp = vpnAccount.getOriginal("api/basic/school/year/task-list?year=$year").bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			val result: JsonArray? = js["result", JSONArray]
			val list = result?.mapNotNull {
				val obj = it.jsonObject
				SportTask(
					name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
					testType = obj["testType"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
					id = obj["id"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
				)
			} ?: emptyList()
			tasks.value = list
			if(list.isNotEmpty()) {
				selectedTask = list[0].id
				queryScores(year, list[0].id)
			}
		}, {
			Logger.e(e = it)
			toastError("获取任务列表失败：${it.message}")
		})
	}

	/**
	 * 查询指定任务下的成绩记录
	 */
	fun queryScores(year: Int, taskId: Int) {
		selectedTask = taskId
		val account = vpnAccount.getAccount()
		request({
			val resp = vpnAccount.getOriginal("api/tzjc/score/upload-list?studentNumber=$account&year=$year&taskId=$taskId").bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			val result: JsonArray? = js["result", JSONArray]
			val list = result?.mapNotNull {
				val obj = it.jsonObject
				SportUpload(
					studentId = obj["studentId"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
					score = obj["score"]?.jsonPrimitive?.contentOrNull ?: "",
					uploadId = obj["uploadId"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
					studentNumber = obj["studentNumber"]?.jsonPrimitive?.contentOrNull ?: "",
					projectNumber = obj["projectNumber"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0,
					testTime = obj["testTime"]?.jsonPrimitive?.long ?: 0L,
					projectName = obj["projectName"]?.jsonPrimitive?.contentOrNull ?: "",
					taskId = obj["taskId"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
				)
			} ?: emptyList()
			scores.value = list
		}, {
			Logger.e(e = it)
			toastError("获取成绩记录失败：${it.message}")
		})
	}

	/**
	 * 查询成绩明细
	 */
	fun queryDetail(year: Int) {
		val account = vpnAccount.getAccount()
		request({
			val resp = vpnAccount.getOriginal("api/tzjc/score/detail?studentNumber=$account&year=$year").bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			val result: JsonArray? = js["result", JSONArray]
			val list = result?.mapNotNull {
				val obj = it.jsonObject
				SportScore(
					avg = obj["avg"]?.jsonPrimitive?.contentOrNull ?: "",
					score2 = obj["score2"]?.jsonPrimitive?.contentOrNull ?: "",
					isCheat = obj["isCheat"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false,
					level = obj["level"]?.jsonPrimitive?.contentOrNull ?: "",
					score1 = obj["score1"]?.jsonPrimitive?.contentOrNull ?: "",
					projectName = obj["projectName"]?.jsonPrimitive?.contentOrNull ?: "",
					percent = obj["percent"]?.jsonPrimitive?.contentOrNull ?: ""
				)
			} ?: emptyList()
			detail.value = list
		}, {
			Logger.e(e = it)
			toastError("获取成绩明细失败：${it.message}")
		})
	}
}
