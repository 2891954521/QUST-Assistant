package com.qust.helper.utils

import android.util.Log


actual fun getLogger(): AbstractLogger = LoggerImpl()

class LoggerImpl: AbstractLogger {

	override fun d(msg: String) {
		Log.d("QustHelper", msg)
	}

	override fun i(msg: String) {
		Log.i("QustHelper", msg)
	}

	override fun w(msg: String) {
		Log.w("QustHelper", msg)
	}

	override fun e(msg: String?, e: Throwable?) {
		Log.e("QustHelper", msg, e)
	}

	override fun log(tag: String, msg: String?, throwable: Throwable?) {
		Log.i(tag, msg, throwable)
	}

}