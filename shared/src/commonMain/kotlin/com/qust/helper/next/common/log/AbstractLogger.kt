package com.qust.helper.next.common.log

abstract class AbstractLogger {

    open val defaultTag: String = "KMP_LOG"

    open fun d(msg: String){
        log(defaultTag, msg)
    }

    open fun i(msg: String){
        log(defaultTag, msg)
    }

    open fun w(msg: String){
        log(defaultTag, msg)
    }

    open fun e(msg: String? = null, e: Throwable? = null){
        log(defaultTag, msg, e)
    }

    abstract fun log(tag: String, msg: String?, throwable: Throwable? = null)
}