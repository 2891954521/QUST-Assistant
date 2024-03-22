package com.qust.helper.model.account

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import com.qust.helper.data.Data
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.api.QustApi
import com.qust.helper.data.eas.Academic
import com.qust.helper.data.eas.Exam
import com.qust.helper.data.eas.Mark
import com.qust.helper.data.eas.Notice
import com.qust.helper.model.Logger
import com.qust.helper.utils.CodeUtils
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Calendar
import java.util.regex.Pattern
import javax.crypto.Cipher

open class EASAccount : Account(
	QustApi.EA_HOSTS[Setting.getInt(Keys.EA_HOST, 0)],
	Keys.EAS_ACCOUNT,
	Keys.EAS_PASSWORD,
	"eaCookie",
	"https"
) {

	companion object {
		fun getInstance(): EASAccount {
			return Instance.INSTANCE;
		}
	}

	private object Instance {
		val INSTANCE = EASAccount()
	}

	private var entranceTimeData = MutableLiveData(Setting.getInt(Keys.ENTRANCE_TIME, -1))

	/**
	 * 入学年份信息
	 */
	var entranceTime: Int
		get() {
			val time = entranceTimeData.value
			return time ?: -1
		}
		set(entranceTime) {
			Setting.edit { it.putInt(Keys.ENTRANCE_TIME, entranceTime) }
			entranceTimeData.postValue(entranceTime)
		}

	open fun changeHost(index: Int) {
		if(index >= QustApi.EA_HOSTS.size) return
		isLogin = false
		cookieJar.clearCookies()
		host = QustApi.EA_HOSTS[index]
		Setting.edit { it.putInt(Keys.EA_HOST, index) }
	}

	override suspend fun absCheckLogin(): Boolean {
		getNoRedirect("jwglxt/xtgl/index_initMenu.html").use {
			return it.code == HttpURLConnection.HTTP_OK
		}
	}

	override suspend fun absLogin(account: String, password: String): Boolean {
		cookieJar.clearCookies()

		val html: String
		getNoCheck(QustApi.EA_LOGIN).use {
			html = it.body?.string() ?: throw IOException("网络错误")
		}

		var token: String? = null
		val matcher = "<input (.*?)>".toPattern().matcher(html)
		while(matcher.find()) {
			val input = matcher.group()
			if(input.contains("csrftoken")) {
				token = CodeUtils.matcher("value=\"(.*?)\"".toPattern(), input) ?: throw IOException("无法获取 csrfToken")
			}
		}
		val csrfToken = token ?: throw IOException("无法获取 csrfToken")

		val publicKey: String
		getNoCheck(QustApi.EA_LOGIN_PUBLIC_KEY).use {
			try {
				publicKey = JSONObject(it.body?.string() ?: throw IOException("无法获取 publicKey")).getString("modulus")
			} catch(e: JSONException) {
				throw IOException("无法获取 publicKey", e)
			}
		}

		val rsaPassword = encrypt(password, publicKey) ?: throw IOException("RSA加密出错")

		postNoRedirect(
			QustApi.EA_LOGIN, FormBody.Builder()
				.add("csrftoken", csrfToken)
				.add("yhm", account)
				.add("mm", rsaPassword)
				.build()
		).use {
			val code: Int = it.code
			return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307
		}
	}

	/**
	 * RSA公钥加密
	 */
	private fun encrypt(str: String, publicKey: String?): String? {
		return try {
			// base64编码的公钥
			val decoded = Base64.decode(publicKey, Base64.DEFAULT)
			val sb = StringBuilder()
			for(b in decoded) {
				val hex = Integer.toHexString(b.toInt() and 0xFF)
				if(hex.length < 2) sb.append(0)
				sb.append(hex)
			}
			val a = BigInteger(sb.toString(), 16)
			val b = BigInteger("65537")
			val pubKey = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(a, b)) as RSAPublicKey
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			cipher.init(Cipher.ENCRYPT_MODE, pubKey)
			Base64.encodeToString(cipher.doFinal(str.toByteArray()), Base64.DEFAULT)
		} catch(e: Exception) {
			null
		}
	}

	/**
	 * 获取当前年级
	 */
	fun getCurrentGrade(): Int {
		val calendar = Calendar.getInstance()
		val y = calendar[Calendar.YEAR]
		return if(y < entranceTime) {
			0
		} else {
			((y - entranceTime) * 2 - if(calendar[Calendar.MONTH] < Calendar.AUGUST) 1 else 0).coerceAtMost(Data.TermName.size - 1)
		}
	}

	/**
	 * 查询成绩
	 * @param xnm  学年代码 20xx
	 * @param xqm  学期代码 12 | 3
	 */
	suspend fun queryMark(xnm: String, xqm: String): Array<Mark> {
		var json: String
		try {
			postNoCheck(QustApi.GET_MARK, FormBody.Builder()
					.add("xnm", xnm).add("xqm", xqm)
					.add("queryModel.showCount", "999").build()
			).use { json = it.body!!.string() }
		} catch(_: Exception) {
			return emptyArray()
		}

		val markMap = HashMap<String, Mark>(8)
		try {
			val item: JSONArray = JSONObject(json).getJSONArray("items")
			for(i in 0 until item.length()) {
				val js: JSONObject = item.getJSONObject(i)
				val name = js.getString("kcmc")
				if(markMap.containsKey(name)) continue
				markMap[name] = Mark.createFromJson(js)
			}
		} catch(e: Exception) {
			Logger.e("url=${QustApi.GET_MARK}, body=${json}", e)
		}

		try {
			postNoCheck(QustApi.GET_MARK_DETAIL, FormBody.Builder()
					.add("xnm", xnm).add("xqm", xqm)
					.add("queryModel.showCount", "999").build()
			).use { json = it.body!!.string() }

			val item = JSONObject(json).getJSONArray("items")
			for(i in 0 until item.length()) {
				val js: JSONObject = item.getJSONObject(i)
				val name = js.getString("kcmc")
				var mark = markMap[name]
				if(mark == null) {
					mark = Mark.createFromJson(js)
					markMap[mark.name] = mark
				}
				mark.addItemMark(js)
			}
		} catch(e: Exception) {
			Logger.e("url=${QustApi.GET_MARK_DETAIL}, body=${json}", e)
		}
		return markMap.values.toTypedArray()
	}

	/**
	 * 查询考试
	 * @param xnm  学年代码 20xx
	 * @param xqm  学期代码 12 | 3
	 */
	suspend fun queryExam(xnm: String, xqm: String): Array<Exam> {
		var json: String
		try {
			postNoCheck(
				QustApi.GET_EXAM, FormBody.Builder()
					.add("xnm", xnm).add("xqm", xqm)
					.add("queryModel.showCount", "999")
					.build()
			).use {
				json = it.body!!.string()
			}
		} catch(_: Exception) {
			return emptyArray()
		}

		val item = JSONObject(json).getJSONArray("items")
		val array = ArrayList<Exam>(item.length())
		for(i in 0 until item.length()){
			array.add(Exam.createFromJson(item.getJSONObject(i)))
		}

		return array.toTypedArray()
	}

	/**
	 * 查询教务通知
	 * @param page         第几页
	 * @param pageSize     每页数量
	 */
	suspend fun queryNotice(page: Int = 1, pageSize: Int = 1): Array<Notice> {
		var json = ""
		try{
			postNoCheck(QustApi.EA_SYSTEM_NOTICE, FormBody.Builder()
					.add("queryModel.showCount", pageSize.toString())
					.add("queryModel.currentPage", page.toString())
					.add("queryModel.sortName", "cjsj")
					.add("queryModel.sortOrder", "desc")
					.build()
			).use { json = it.body!!.string() }
		}catch(e: Exception){
			Logger.e("'url:'${QustApi.EA_SYSTEM_NOTICE}\n$json", e)
			return emptyArray()
		}

		val array: ArrayList<Notice> = ArrayList()
		if(json.startsWith("{") || json.startsWith("[")) {
			val item = JSONObject(json).getJSONArray("items")
			for(i in 0 until item.length()) array.add(Notice.createFromJson(item.getJSONObject(i)))
		}
		return array.toArray(arrayOfNulls<Notice>(0))
	}


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
	suspend fun getAcademic(): Pair< Array<Academic.LessonInfoGroup>, Array<Academic.LessonInfo>> {

		// 查询到的所有课程
		val lessonInfo = ArrayList<Academic.LessonInfo>(64)

		// 储存所有课程的分组
		val xfyqjd = HashMap<String, Academic.LessonInfoGroup.Builder>()

		try{
			var html: String
			getNoCheck(QustApi.ACADEMIC_PAGE).use { html = it.body!!.string() }

			var matcher = xfyqjd_id.matcher(html)
			while(matcher.find()){
				val id = matcher.group(1)!!
				if(!xfyqjd.containsKey(id)) xfyqjd[id] = Academic.LessonInfoGroup.Builder()
			}

			matcher = xfyqjd_id_yxxf_yqzdxf.matcher(html)
			while(matcher.find()){
				val id = matcher.group(1)!!
				if(xfyqjd.containsKey(id)){
					val group = xfyqjd[id]!!
					group.obtainedCredits = matcher.group(2)?.toFloatOrNull() ?: 0F
					group.requireCredits = matcher.group(3)?.toFloatOrNull() ?: 0F
				}
			}

			for(param in xfyqjd.keys) {
				postNoCheck(QustApi.ACADEMIC_INFO, FormBody.Builder()
					.add("xfyqjd_id", param)
					.add("xh_id", accountName).build()
				).use { html = it.body!!.string() }

				val array = JSONArray(html)
				if(array.length() == 0) continue

				val group = xfyqjd[param]!!
				for(i in 0 until array.length()) {
					val info = Academic.LessonInfo.createFromJson(array.getJSONObject(i))
					when(info.status){
						// 统计未过课程的学分
						2 -> { group.creditNotEarned += info.credit.toFloatOrNull() ?: 0F }
						// 统计已修门数
						4 -> { group.passedCounts++ }
					}
					// 指向课程的索引
					group.addLesson(lessonInfo.size)
					lessonInfo.add(info)
				}
				group.groupName = lessonInfo[lessonInfo.size - 1].category
			}

			val array = ArrayList<Academic.LessonInfoGroup>(xfyqjd.size)
			xfyqjd.values.forEach{
				if(it.hasLesson()) array.add(it.build())
			}

			return Pair(array.toTypedArray(), lessonInfo.toTypedArray())
		}catch(e: Exception){
			Logger.e(e)
		}

		return Pair(emptyArray(), emptyArray())
	}
}