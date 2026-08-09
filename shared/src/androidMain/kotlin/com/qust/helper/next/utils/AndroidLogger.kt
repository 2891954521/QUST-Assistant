package com.qust.helper.next.utils

import android.util.Log
import com.qust.helper.next.common.log.AbstractLogger
import com.qust.helper.next.utils.CodeUtils
import com.qust.helper.next.utils.DateUtils
import com.qust.helper.next.BaseApplication
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class AndroidLogger: AbstractLogger {

    var LogFile: String? = null

    constructor(application: BaseApplication) {
        val f = application.getExternalFilesDir("log")
        if(!f!!.exists()) f.mkdirs()
        LogFile = f.toString()
    }

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
        if(e != null) logError(e)
    }

    override fun log(tag: String, msg: String?, throwable: Throwable?) {
        Log.i(tag, msg, throwable)
        if(throwable != null) logError(throwable)
    }

    /**
     * 强制输出到 debug 文件
     */
    fun debugLog(string: String) {
        FileUtils.appendFile(File(LogFile, "debug.log"), string + "\n")
    }

    /**
     * Log 输出异常
     * @param e 异常
     * @param catchBy 异常是被谁捕获的
     * @param level 异常等级
     * @return 错误代码
     */
    fun logError(e: Throwable, catchBy: String? = null, level: Int = 2): String {

        val stringWriter = StringWriter()

        e.printStackTrace(PrintWriter(stringWriter))

        val str = stringWriter.toString()
        val hash = CodeUtils.md5(str)

        val f = File(LogFile, "$hash.log")

        if(f.exists()) {
            debugLog("${DateUtils.currentTime()} 发生异常:$hash")
        }else{
            debugLog("${DateUtils.currentTime()} 发生异常: $hash\n ${e.javaClass.name} : ${e.message}")
            FileUtils.writeFile(f.toString(), str)
        }
        return hash.substring(0, 3) + hash.substring(hash.length - 3)
    }

}