package com.qust.helper.viewmodel.business

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.business.Electricity
import com.qust.helper.entity.business.Node
import com.qust.helper.model.network.NeedLoginException
import com.qust.helper.model.account.VpnAccount
import com.qust.helper.utils.CodeUtils
import com.qust.helper.utils.JSONArray
import com.qust.helper.utils.JSONObject
import com.qust.helper.utils.JsonUtils
import com.qust.helper.utils.JsonUtils.get
import com.qust.helper.utils.Logger
import com.qust.helper.utils.SettingUtils
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK
import com.qust.helper.viewmodel.extend.toastWarning
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * 电费充值 ViewModel
 */
class ElectricRechargeViewModel : RequestViewModel() {

	private val PARAMS = arrayOf(
		arrayOf("query_applist",        "applist",      "aid",      "",         "aid"),
		arrayOf("query_elec_area",      "areatab",      "area",     "areaname", "area"),
		arrayOf("query_elec_building",  "buildingtab",  "building", "building", "buildingid"),
		arrayOf("query_elec_floor",     "floortab",     "floor",    "floor",    "floorid"),
		arrayOf("query_elec_room",      "roomtab",      "room",     "room",     "roomid")
	)
	private val FUN_NAME = arrayOf(
		"synjones.onecard.query.applist",
		"synjones.onecard.query.elec.area",
		"synjones.onecard.query.elec.building",
		"synjones.onecard.query.elec.floor",
		"synjones.onecard.query.elec.room"
	)

	private val TICKET_PATTERN = Regex("\\?ticket=([\\da-zA-Z]+)")
	private val SSO_TICKET_ID_PATTERN = Regex("id=\"ssoticketid\" value=\"([\\da-zA-Z]+)\"")

	private val TSM = "/web/Common/Tsm.html"
	private val APP_LIST = "/web/NetWork/AppList.html"

	private val vpnAccount = VpnAccount("df.qust.edu.cn", "http")

	var needLogin by mutableStateOf(false)

	val rooms = mutableStateListOf<Electricity>()

	var account by mutableStateOf("")
	var balance by mutableFloatStateOf(0F)

	private var hasLogin = false
	private var isSSOLogin = false

	private val nodes: Node = Node()
	private val selectList: ArrayList<Int> = ArrayList()

	init {
		val saved = SettingUtils.getString("electric_rooms")
		if(saved.isNotEmpty()) {
			try {
				rooms.addAll(Json.decodeFromString(ListSerializer(Electricity.serializer()), saved))
			} catch(e: Exception) {
				Logger.e(e = e)
			}
		}
	}

	/**
	 * 检查是否选择了上一级
	 */
	fun checkIndex(index: Int): Boolean {
		return if(index > selectList.size) {
			toastWarning("请先选择上一级")
			false
		} else {
			true
		}
	}

	/**
	 * 获取某一级下的文本内容
	 */
	fun getIndexName(index: Int): Array<String> {
		var list: Array<Node> = nodes.child
		for(i in 0 until index) {
			list = list[selectList[i]].child
		}
		return list.map { it.name }.toTypedArray()
	}

	/**
	 * 选择一个节点
	 */
	fun chooseNode(index: Int, position: Int) {
		if(index > selectList.size) {
			toastWarning("请先选择上一级")
			return
		}
		selectList.subList(index, selectList.size).clear()
		selectList.add(position)
		if(selectList.size < 5) {
			var list: Array<Node> = nodes.child
			for(i in 0 until index) list = list[selectList[i]].child
			val currentNode = list[position]
			if(currentNode.child.isEmpty()) queryNode(currentNode)
		}
	}

	fun addRoom() {
		if(selectList.size < 5) {
			toastWarning("请选择所有选项")
		} else {
			var node: Node = nodes
			val id = ArrayList<String>(selectList.size)
			val name = ArrayList<String>(selectList.size)
			for(i in selectList.indices) {
				node = node.child[selectList[i]]
				id.add(node.nodeId)
				name.add(node.name)
			}
			val room = Electricity(
				id = id.toTypedArray(),
				name = name.toTypedArray(),
				roomName = node.name
			)
			rooms.add(room)
			saveData()
		}
	}

	fun deleteRoom(index: Int) {
		rooms.removeAt(index)
		saveData()
	}

	/**
	 * 检查并获取第0级信息
	 */
	fun checkNode() {
		if(nodes.child.isNotEmpty()) return
		request({
			if(!hasLogin && !getCardInfo()) return@request
			val resp = post(APP_LIST, """{"query_applist":{"apptype":"elec"}}""", "synjones.onecard.query.applist").bodyAsText()
			val js = JsonUtils.parseString<JsonObject>(resp)
			val appList = js["query_applist", JSONObject]?.get("applist", JSONArray)?.jsonArray ?: return@request
			val nodeAid = ArrayList<Node>(appList.size)
			for(i in 0 until appList.size) {
				val obj = appList[i].jsonObject
				nodeAid.add(Node(
					obj["aid"]?.jsonPrimitive?.contentOrNull ?: "",
					obj["name"]?.jsonPrimitive?.contentOrNull ?: ""
				))
			}
			nodes.child = nodeAid.toTypedArray()
		}, {
			toastError("获取电控信息失败: ${it.message}")
		})
	}

	fun refreshCard() {
		request({
			getCardInfo()
		}, {
			toastError("获取卡信息失败: ${it.message}")
		})
	}

	fun refreshBalance(index: Int) {
		request({
			if(!hasLogin && !getCardInfo()) return@request
			try {
				val room = rooms[index]
				val query = buildJsonObject {
					put("account", account)
					put("aid", room.id[0])
					put("extdata", "info1=")
					for(i in 1 until room.id.size) {
						put(PARAMS[i][2], buildJsonObject {
							put(PARAMS[i][3], room.name[i])
							put(PARAMS[i][4], room.id[i])
						})
					}
				}
				val resp = post(TSM, buildJsonObject {
					put("query_elec_roominfo", query)
				}.toString(), "synjones.onecard.query.elec.roominfo").bodyAsText()
				val js = JsonUtils.parseString<JsonObject>(resp)
				val errmsg = js["query_elec_roominfo", JSONObject]?.get("errmsg", "") ?: ""
				val matcher = Regex("[0-9.]+").find(errmsg)
				if(matcher != null) {
					rooms[index] = room.copy(balance = matcher.value.toFloat())
					saveData()
				} else {
					toastError("查询失败")
				}
			} catch(e: Exception) {
				Logger.e(e = e)
				toastError("查询失败: ${e.message}")
			}
		})
	}

	fun recharge(roomIndex: Int, amount: Int) {
		if(amount == 0) {
			toastWarning("请输入充值金额")
			return
		}
		val room = rooms[roomIndex]
		request({
			if(!hasLogin && !getCardInfo()) return@request
			if(!isSSOLogin) {
				try {
					var ticket: String
					val preResp: HttpResponse = vpnAccount.getFullUrlNoCheck("http://211.87.155.80:7280/ias/prelogin?sysid=FWDT")
					val preHtml = preResp.bodyAsText()
					ticket = CodeUtils.matcher(SSO_TICKET_ID_PATTERN, preHtml) ?: throw Exception("单点登陆失败，无法获取ticket")

					val ssoResp: HttpResponse = vpnAccount.postFullUrlNoCheck("http://211.87.155.92:8080/cassyno/index", mapOf(
						"errorcode" to "1",
						"continueurl" to "",
						"ssoticketid" to ticket
					))
					ssoResp.bodyAsText()

					val pageResp: HttpResponse = vpnAccount.postFullUrlNoCheck("http://211.87.155.92:8080/Page/Page", mapOf(
						"flowID" to "151",
						"type" to "1",
						"apptype" to "4",
						"Url" to "http%3a%2f%2fdf.qust.edu.cn%2fweb%2fcommon%2fcheckEle.html"
					))
					val pageHtml = pageResp.bodyAsText()
					ticket = CodeUtils.matcher(TICKET_PATTERN, pageHtml) ?: throw Exception("登陆失败，无法获取ticket")
					val checkResp: HttpResponse = vpnAccount.getFullUrlNoCheck("/web/common/checkEle.html?ticket=$ticket")
					checkResp.bodyAsText()
					isSSOLogin = true
				} catch(e: Exception) {
					isSSOLogin = false
					Logger.e(e = e)
					throw e
				}
			}
			try {
				val resp = vpnAccount.postNoCheck("/web/Elec/PayElecGdc.html", mapOf(
					"acctype" to "###",
					"json" to "true",
					"paytype" to "1",
					"qpwd" to "",
					"account" to account,
					"tran" to amount.toString(),
					"aid" to room.id[0],
					"roomid" to room.id[4],
					"room" to room.name[4]
				)).bodyAsText()
				val js = JsonUtils.parseString<JsonObject>(resp)
				val errmsg = js["pay_elec_gdc", JSONObject]?.get("errmsg", "") ?: ""
				toastOK(errmsg)
			} catch(e: Exception) {
				Logger.e(e = e)
				toastError("充值失败：${e.message}")
			}
		}, {
			toastError("充值失败：${it.message}")
		})
	}

	private fun saveData() {
		try {
			SettingUtils.putString("electric_rooms", Json.encodeToString(ListSerializer(Electricity.serializer()), rooms.toList()))
		} catch(e: Exception) {
			Logger.e(e = e)
		}
	}

	/**
	 * 查询当前选中的节点的信息
	 */
	private fun queryNode(currentNode: Node) {
		request({
			if(!hasLogin && !getCardInfo()) return@request
			try {
				var node: Node = nodes.child[selectList[0]]
				val query = buildJsonObject {
					put("account", account)
					put("aid", node.nodeId)
					val index = selectList.size
					for(i in 1 until index) {
						node = node.child[selectList[i]]
						put(PARAMS[i][2], buildJsonObject {
							put(PARAMS[i][3], node.name)
							put(PARAMS[i][4], node.nodeId)
						})
					}
				}
				val resp = post(TSM, buildJsonObject {
					put(PARAMS[selectList.size][0], query)
				}.toString(), FUN_NAME[selectList.size]).bodyAsText()
				val js = JsonUtils.parseString<JsonObject>(resp)
				val name: String = PARAMS[selectList.size][3]
				val id: String = PARAMS[selectList.size][4]
				val array = js[PARAMS[selectList.size][0], JSONObject]?.get(PARAMS[selectList.size][1], JSONArray)?.jsonArray ?: return@request
				val nodeAid = ArrayList<Node>(array.size)
				for(i in 0 until array.size) {
					val obj = array[i].jsonObject
					nodeAid.add(Node(
						obj[id]?.jsonPrimitive?.contentOrNull ?: "",
						obj[name]?.jsonPrimitive?.contentOrNull ?: ""
					))
				}
				nodeAid.sortBy { it.name }
				currentNode.child = nodeAid.toTypedArray()
			} catch(e: Exception) {
				Logger.e(e = e)
				toastError("获取电控信息失败: ${e.message}")
			}
		})
	}

	private suspend fun getCardInfo(): Boolean {
		try {
			vpnAccount.checkLogin()
		} catch(e: NeedLoginException) {
			toastWarning("请先登录")
			needLogin = true
			return false
		} catch(e: Exception) {
			Logger.e(e = e)
			toastError("网络错误")
			return false
		}

		try {
			val resp = post(TSM, """{"query_card":{"idtype":"sno","id":"${vpnAccount.getAccount()}"}}""", "synjones.onecard.query.card").bodyAsText()
			var js = JsonUtils.parseString<JsonObject>(resp)
			val cards = js["query_card", JSONObject]?.get("card", JSONArray)?.jsonArray
			if(cards == null || cards.isEmpty()) {
				toastError("用户没有绑卡，无法使用该功能")
				return false
			} else if(cards.size > 1) {
				toastWarning("用户有多张卡，默认使用第一张")
			}
			val card = cards[0].jsonObject
			account = card["account"]?.jsonPrimitive?.contentOrNull ?: ""
		} catch(e: Exception) {
			Logger.e(e = e)
			toastError("获取卡信息失败")
			return false
		}

		try {
			val resp = vpnAccount.postFullUrlNoCheck("https://i.qust.edu.cn/tp_up/up/subgroup/queryCardBalance", mapOf()).bodyAsText()
			balance = resp.toFloatOrNull() ?: Float.NaN
		} catch(e: Exception) {
			Logger.e(e = e)
		}

		hasLogin = true
		return true
	}

	private suspend fun post(url: String, payload: String, funName: String): io.ktor.client.statement.HttpResponse {
		return vpnAccount.postNoCheck(url, mapOf(
			"json" to "true",
			"jsondata" to payload,
			"funname" to funName
		))
	}
}
