package com.qust.helper.utils

interface AbstractLogger {

	fun d(msg: String){
		log("QustHelper", msg)
	}

	fun i(msg: String){
		log("QustHelper", msg)
	}

	fun w(msg: String){
		log("QustHelper", msg)
	}

	fun e(msg: String? = null, e: Throwable? = null){
		log("QustHelper", msg, e)
	}

	fun log(tag: String, msg: String?, throwable: Throwable? = null)
}

expect fun getLogger(): AbstractLogger

object Logger: AbstractLogger by getLogger()