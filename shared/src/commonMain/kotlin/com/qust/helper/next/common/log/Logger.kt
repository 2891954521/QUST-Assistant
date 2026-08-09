package com.qust.helper.next.common.log

expect fun platformLogger(): AbstractLogger

object Logger {

	var baseLogger = platformLogger()

	fun d(any: Any?) = baseLogger.d(any.toString())
	fun d(vararg args: Any?) = baseLogger.d(args.joinToString(", "))
	fun d(msg: String) = baseLogger.d(msg)

	fun i(any: Any?) = baseLogger.i(any.toString())
	fun i(vararg args: Any?) = baseLogger.i(args.joinToString(", "))
	fun i(msg: String) = baseLogger.i(msg)

	fun w(any: Any?) = baseLogger.w(any.toString())
	fun w(vararg args: Any?) = baseLogger.w(args.joinToString(", "))
	fun w(msg: String) = baseLogger.w(msg)

	fun e(msg: String) = baseLogger.e(msg = msg)
	fun e(e: Throwable) = baseLogger.e(e = e)
	fun e(msg: String, e: Throwable) = baseLogger.e(msg, e)

}