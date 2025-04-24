package com.qust.helper.utils

import java.io.PrintWriter
import java.io.StringWriter


actual fun getLogger(): AbstractLogger = LoggerImpl()

class LoggerImpl: AbstractLogger {

	override fun d(msg: String) {
		println("[QustHelper]\u001b[36m[D]\u001b[0m $msg")
	}

	override fun i(msg: String) {
		println("[QustHelper]\u001b[32m[I]\u001b[0m $msg")
	}

	override fun w(msg: String) {
		println("[QustHelper]\u001b[33m[W]\u001b[0m $msg")
	}

	override fun e(msg: String?, e: Throwable?) {
		if(e == null){
			println("[QustHelper]\u001b[31m[E]\u001b[0m $msg")
		}else{
			val writer = StringWriter()
			e.printStackTrace(PrintWriter(writer))
			println("[QustHelper]\u001b[31m[E]\u001b[0m $msg ${e.message}\n $writer")
		}
	}

	override fun log(tag: String, msg: String?, throwable: Throwable?) {
		if(throwable == null){
			println("[${tag}]\u001b[31m[E]\u001b[0m $msg")
		}else{
			val writer = StringWriter()
			throwable.printStackTrace(PrintWriter(writer))
			println("[QustHelper]\u001b[31m[E]\u001b[0m $msg ${throwable.message}\n $writer")
		}
	}

}