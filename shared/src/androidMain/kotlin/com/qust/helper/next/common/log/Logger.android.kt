package com.qust.helper.next.common.log

import android.util.Log

actual fun platformLogger(): AbstractLogger = AndroidLogger()

class AndroidLogger: AbstractLogger() {

    override fun d(msg: String) {
        Log.d(defaultTag, msg)
    }

    override fun i(msg: String) {
        Log.i(defaultTag, msg)
    }

    override fun w(msg: String) {
        Log.w(defaultTag, msg)
    }

    override fun e(msg: String?, e: Throwable?) {
        Log.e(defaultTag, msg, e)
    }

    override fun log(tag: String, msg: String?, throwable: Throwable?) {
        Log.i(tag, msg, throwable)
    }
}