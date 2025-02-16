package com.qust.helper.model

import android.util.Log
import com.qust.helper.App
import com.qust.helper.data.Setting
import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.FileUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date

object Logger {

	// Log路径
	var LogFile: String? = null

	private var DEBUG = false

	private var UPLOAD_LOG = false

	/**
	 * 初始化日志模块并处理日志上报
	 */
	fun init(app: App) {
		DEBUG = Setting.getBoolean("key_debug")
		val f = app.getExternalFilesDir("log")
		if(!f!!.exists()) f.mkdirs()
		LogFile = f.toString()
	}

	fun d(msg: String, e: Throwable? = null){
		Log.d("QustHelper", msg, e)
	}

	fun i(msg: String, e: Throwable? = null){
		Log.i("QustHelper", msg, e)
	}

	fun w(msg: String, e: Throwable? = null){
		Log.w("QustHelper", msg, e)
	}

	fun e(msg: String, e: Throwable? = null){
		Log.e("QustHelper", msg, e)
	}

	fun e(e: Throwable){
		Log.e("QustHelper", "", e)
	}

	/**
	 * 调试模式下才会进行输出
	 * @param msg message
	 */
	fun Log(msg: String) {
		if(DEBUG) {
			android.util.Log.i("i", msg)
			debugLog(msg)
		}
	}

	/**
	 * 强制输出到 debug 文件
	 */
	fun debugLog(string: String) {
		FileUtils.appendFile(File(LogFile, "debug.log"), string + "\n")
	}

	/**
	 * Log 输出异常
	 */
	fun Log(e: Throwable) {
		Log(e, UPLOAD_LOG)
	}

	/**
	 * Log 输出异常
	 */
	fun Log(s: String?, e: Throwable) {
		Log(s, e, UPLOAD_LOG)
	}

	/**
	 * Log 输出异常
	 * @param e 异常
	 * @param needUpload 是否需要上报
	 */
	fun Log(e: Throwable, needUpload: Boolean) {
		Log(null, e, needUpload)
	}

	/**
	 * Log 输出异常
	 * @param msg 附加错误信息
	 * @param e 异常
	 * @param needUpload 是否需要上报
	 */
	fun Log(msg: String?, e: Throwable, needUpload: Boolean) {
		val stringWriter = StringWriter()
		e.printStackTrace(PrintWriter(stringWriter))
		var str = stringWriter.toString()
		if(msg != null) str += if(msg.length > 4096) msg.substring(0, 4096) else msg
		val hash = str.hashCode()
		val f = File(LogFile, "$hash.log")
		if(f.exists()) {
			debugLog(DateUtils.YMD_HM.format(Date(System.currentTimeMillis())) + " 发生异常:" + hash)
			if(needUpload) {
				try {
					FileUtils.appendFileNewLine(File(LogFile, "upload.log"), JSONObject().put("hash", hash).toString())
				} catch(ignored: JSONException) {
				}
			}
		} else {
			FileUtils.writeFile(f.toString(), str)
			val message = e.javaClass.name + " : " + e.message
			if(needUpload) {
				try {
					FileUtils.appendFileNewLine(File(LogFile, "upload.log"), JSONObject().put("hash", hash).put("message", message).toString())
				} catch(ignored: JSONException) { }
			}
			debugLog(DateUtils.YMD_HM.format(Date(System.currentTimeMillis())) + " 发生异常:" + hash + "\n" + message)
		}
	}
}