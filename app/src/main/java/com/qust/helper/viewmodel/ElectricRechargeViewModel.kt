package com.qust.helper.viewmodel

import android.app.Application
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Electricity
import com.qust.helper.model.Logger
import com.qust.helper.model.account.NeedLoginException
import com.qust.helper.model.account.VpnAccount
import com.qust.helper.ui.page.business.ElectricRecharge.TITLE
import com.qust.helper.ui.widget.DialogAble
import com.qust.helper.ui.widget.DialogAbleImpl
import com.qust.helper.ui.widget.ToastAble
import com.qust.helper.ui.widget.ToastAbleImpl
import com.qust.helper.ui.widget.ToastContent
import com.qust.helper.utils.CodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

@OptIn(ExperimentalSerializationApi::class)
class ElectricRechargeViewModel(application: Application): AndroidViewModel(application),
	ToastAble by ToastAbleImpl(),
	DialogAble by DialogAbleImpl() {

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

	private val TICKET_PATTERN = Pattern.compile("\\?ticket=([\\da-zA-Z]+)")
	private val SSO_TICKET_ID_PATTERN = Pattern.compile("id=\"ssoticketid\" value=\"([\\da-zA-Z]+)\"")

	private val TSM = "/web/Common/Tsm.html"
	private val APP_LIST = "/web/NetWork/AppList.html"

	private val vpnAccount = VpnAccount("df.qust.edu.cn", "http")

	val uiState = ElectricUIState(
		dialogText = _dialogText,
		toastContent = toastContent
	)

	val uiEvent = object : ElectricUIEvent{
		override fun refreshCard() { this@ElectricRechargeViewModel.refreshCard() }

		override fun checkIndex(index: Int): Boolean { return this@ElectricRechargeViewModel.checkIndex(index) }
		override fun getIndexName(index: Int): Array<String> { return this@ElectricRechargeViewModel.getIndexName(index) }

		override fun checkNode() { this@ElectricRechargeViewModel.checkNode() }
		override fun chooseNode(index: Int, choose: Int) { this@ElectricRechargeViewModel.chooseNode(index, choose) }

		override fun refreshBalance(roomIndex: Int) { this@ElectricRechargeViewModel.refreshBalance(roomIndex) }
		override fun recharge(roomIndex: Int, amount: Int) { this@ElectricRechargeViewModel.recharge(roomIndex, amount) }

		override fun addRoom() { this@ElectricRechargeViewModel.addRoom() }
		override fun deleteRoom(roomIndex: Int) { this@ElectricRechargeViewModel.deleteRoom(roomIndex) }
	}

	private val dataPath: File

	private var hasLogin = false
	private var isSSOLogin = false

	private val nodes: Node = Node()
	private val selectList: ArrayList<Int> = ArrayList()
	
	init{
		dataPath = File(application.filesDir, "electric")
		if(dataPath.exists()) {
			try {
				FileInputStream(dataPath).use {
					uiState.rooms.addAll(Json.decodeFromStream<Array<Electricity>>(it))
				}
			}catch(_: Exception){ }
		}
	}

	/**
	 * 检查是否选择了上一级
	 */
	fun checkIndex(index: Int): Boolean{
		return if(index > selectList.size) {
			toastWarning("请先选择上一级")
			false
		}else{
			true
		}
	}

	/**
	 * 获取某一级下的文本内容
	 */
	fun getIndexName(index: Int): Array<String>{
		// 定位到这一级
		var list: Array<Node> = nodes.child
		for(i in 0 until index) {
			list = list[selectList[i]].child
		}
		// 获取这一级的所有选项的名字
		return list.map { it.name }.toTypedArray()
	}

	/**
	 * 选择一个节点
	 */
	fun chooseNode(index: Int, position: Int){
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

	fun addRoom(){
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
				name = id.toTypedArray(),
				roomName = node.name
			)
			uiState.rooms.add(room)
			saveData()
		}
	}

	fun deleteRoom(index: Int){
		uiState.rooms.removeAt(index)
		saveData()
	}

	/**
	 * 检查并获取第0级信息
	 */
	fun checkNode(){
		if(nodes.child.isNotEmpty()) return
		viewModelScope.launch {
			showDialog("正在获取电控信息")
			withContext(Dispatchers.IO){
				if(!hasLogin && !getCardInfo()) return@withContext
				post(APP_LIST, """{"query_applist":{"apptype":"elec"}}""", "synjones.onecard.query.applist").use {
					try {
						val js = JSONObject(it.body!!.string())
						val appList = js.getJSONObject("query_applist").getJSONArray("applist")
						val nodeAid = ArrayList<Node>(appList.length())
						for(i in 0 until appList.length()) {
							val obj = appList.getJSONObject(i)
							val node = Node(
								obj.getString("aid"),
								obj.getString("name")
							)
							nodeAid.add(node)
						}
						nodes.child = nodeAid.toTypedArray()
					} catch(e: Exception) {
						toastError("获取电控信息失败: " + e.message)
					}
				}
			}
			clearDialog()
		}
	}

	fun refreshCard(){
		viewModelScope.launch {
			showDialog("正在获取卡信息")
			withContext(Dispatchers.IO){ getCardInfo() }
			clearDialog()
		}
	}

	fun refreshBalance(index: Int) {
		viewModelScope.launch {
			showDialog("正在刷新")
			withContext(Dispatchers.IO) {
				if(!hasLogin && !getCardInfo()) return@withContext
				try {
					val room = uiState.rooms[index]
					val query: JSONObject = JSONObject().put("account", uiState.account).put("aid", room.id[0]).put("extdata", "info1=")
					for(i in 1 until room.id.size) query.put(PARAMS[i][2], JSONObject().put(PARAMS[i][3], room.name[i]).put(PARAMS[i][4], room.id[i]))
					post(TSM, JSONObject().put("query_elec_roominfo", query).toString(), "synjones.onecard.query.elec.roominfo").use {
						val js: JSONObject = JSONObject(it.body!!.string()).getJSONObject("query_elec_roominfo")
						val matcher: Matcher = "[0-9\\\\.]+".toPattern().matcher(js.getString("errmsg"))
						if(matcher.find()){
							uiState.rooms[index] = room.copy(balance = matcher.group().toFloat())
							saveData()
						} else{
							toastError("查询失败")
						}
					}
				} catch(e: Exception) {
					toastError("查询失败: ${e.message}")
				}
			}
			clearDialog()
		}
	}

	fun recharge(roomIndex: Int, amount: Int) {
		if(amount == 0){
			toastWarning("请输入充值金额")
		}
		val room = uiState.rooms[roomIndex]
		viewModelScope.launch {
			showDialog("正在充值")
			withContext(Dispatchers.IO) {
				if(!hasLogin && !getCardInfo()) return@withContext
				if(!isSSOLogin) {
					try {
						var ticket: String
						vpnAccount.getNoCheck("http-7280", "211.87.155.80", "/ias/prelogin?sysid=FWDT").use {
							ticket = CodeUtils.matcher(SSO_TICKET_ID_PATTERN, it.body!!.string()) ?: throw IOException("单点登陆失败，无法获取ticket")
						}

						showDialog("正在进行单点登录")
						vpnAccount.postNoCheck("http-8080", "211.87.155.92", "/cassyno/index",
							FormBody.Builder().add("errorcode", "1").add("continueurl", "").add("ssoticketid", ticket).build()
						).close()

						showDialog("正在登录到电费平台")
						vpnAccount.postNoCheck("http-8080", "211.87.155.92", "/Page/Page",
							FormBody.Builder().add("flowID", "151").add("type", "1").add("apptype", "4").add("Url", "http%3a%2f%2fdf.qust.edu.cn%2fweb%2fcommon%2fcheckEle.html").build()
						).use {
							ticket = CodeUtils.matcher(TICKET_PATTERN, it.body!!.string()) ?: throw IOException("登陆失败，无法获取ticket")
						}
						vpnAccount.getNoCheck("/web/common/checkEle.html?ticket=$ticket").close()
						isSSOLogin = true
					} catch(e: Exception) {
						isSSOLogin = false
						throw e
					}
				}
				showDialog("正在充值")
				try {
					vpnAccount.postNoCheck("/web/Elec/PayElecGdc.html", FormBody.Builder()
							.add("acctype", "###").add("json", "true")
							.add("paytype", "1").add("qpwd", "")
							.add("account", uiState.account).add("tran", amount.toString())
							.add("aid", room.id[0])
							.add("roomid", room.id[4]).add("room", room.name[4])
							.build()
					).use { response ->
						val js: JSONObject = JSONObject(response.body!!.string()).getJSONObject("pay_elec_gdc")
						toastOK(js.getString("errmsg"))
					}
				} catch(e: Exception) {
					toastError("充值失败：${e}")
				}
			}
			clearDialog()
		}
	}

	private fun saveData(){
		try {
			FileOutputStream(dataPath).use { Json.encodeToStream(uiState.rooms.toTypedArray(), it) }
		} catch(e: IOException) {
			Logger.e(e)
		}
	}

	/**
	 * 查询当前选中的节点的信息
	 */
	private fun queryNode(currentNode: Node) {
		viewModelScope.launch {
			showDialog("正在查询" + TITLE[selectList.size])
			withContext(Dispatchers.IO) {
				if(!hasLogin && !getCardInfo()) return@withContext
				try {
					var node: Node = nodes.child[selectList[0]]
					val query: JSONObject = JSONObject().put("account", uiState.account).put("aid", node.nodeId)
					val index = selectList.size
					for(i in 1 until index) {
						node = node.child[selectList[i]]
						query.put(PARAMS[i][2], JSONObject().put(PARAMS[i][3], node.name).put(PARAMS[i][4], node.nodeId))
					}
					post(TSM, JSONObject().put(PARAMS[index][0], query).toString(), FUN_NAME[selectList.size]).use {
						val js = JSONObject(it.body!!.string())
						val name: String = PARAMS[index][3]
						val id: String = PARAMS[index][4]
						val array = js.getJSONObject(PARAMS[index][0]).getJSONArray(PARAMS[index][1])
						val nodeAid = ArrayList<Node>(array.length())
						for(i in 0 until array.length()) {
							val obj = array.getJSONObject(i)
							nodeAid.add(Node(obj.getString(id), obj.getString(name)))
						}
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							nodeAid.sortWith(Comparator.comparing { o: Node -> o.name })
						}
						currentNode.child = nodeAid.toTypedArray()
					}
				} catch(e: Exception) {
					toastError("获取" + TITLE[selectList.size] + "信息失败: " + e.message)
				}
			}
			clearDialog()
		}
	}

	private suspend fun getCardInfo(): Boolean{
		try {
			vpnAccount.checkLogin()
		}catch(e: NeedLoginException){
			toastWarning("请先登录")
			uiState.needLogin = true
			return false
		}catch(_: Exception){
			toastError("网络错误")
			return false
		}

		post(TSM, """{"query_card":{"idtype":"sno","id":"${vpnAccount.getAccount()}"}}""", "synjones.onecard.query.card").use {
			try {
				var js = JSONObject(it.body!!.string())
				val cards = js.getJSONObject("query_card").getJSONArray("card")
				if(cards.length() == 0) {
					toastError("用户没有绑卡，无法使用该功能")
					return false
				} else if(cards.length() > 1) {
					toastWarning("用户有多张卡，默认使用第一张")
				}
				js = cards.getJSONObject(0)
				if(js.has("account")) uiState.account = js.getString("account")
			} catch(e: Exception) {
				toastError("获取卡信息失败")
				return false
			}
		}

		vpnAccount.postNoCheck(
			"https",
			"i.qust.edu.cn",
			"/tp_up/up/subgroup/queryCardBalance",
			"{}".toRequestBody("application/json".toMediaType())
		).use { resp ->
			uiState.balance = resp.body!!.string().toFloatOrNull() ?: Float.NaN
		}

		hasLogin = true
		return true
	}

	private suspend fun post(url: String, payload: String, funName: String): Response {
		return vpnAccount.postNoCheck(url, FormBody.Builder()
			.add("json", "true")
			.add("jsondata", payload)
			.add("funname", funName).build()
		)
	}
}

@Serializable
class Node(
	val nodeId: String = "",
	val name: String = "",
	var child: Array<Node> = emptyArray()
)

class ElectricUIState(
	var dialogText: MutableState<String>,
	var toastContent: MutableState<ToastContent>
) {
	var needLogin by mutableStateOf(false)

	val rooms = mutableStateListOf<Electricity>()

	var account by mutableStateOf("")
	var balance by mutableFloatStateOf(0F)
}

interface ElectricUIEvent{
	fun refreshCard(){ }

	fun checkIndex(index: Int): Boolean
	fun getIndexName(index: Int): Array<String>

	fun checkNode(){ }
	fun chooseNode(index: Int, choose: Int){ }

	fun refreshBalance(roomIndex: Int){ }
	fun recharge(roomIndex: Int, amount: Int){ }

	fun addRoom(){ }
	fun deleteRoom(roomIndex: Int){ }
}