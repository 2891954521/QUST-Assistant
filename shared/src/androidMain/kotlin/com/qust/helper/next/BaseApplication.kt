package com.qust.helper.next

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.common.setting.AndroidAppSetting
import com.qust.helper.next.ui.router.ActivityCollector
import com.qust.helper.next.utils.DateUtils
import com.qust.helper.next.utils.AndroidLogger
import com.qust.helper.next.utils.CodeUtils
import com.qust.helper.next.utils.FileUtils
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

open class BaseApplication: Application() {

    companion object {
        lateinit var INSTANCE: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        AndroidAppSetting.defaultSharedPreferences = getSharedPreferences("defaultSharedPreferences", MODE_PRIVATE)

        ActivityCollector.context = this

        val logger = AndroidLogger(this)
        Logger.baseLogger = logger

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this, logger, Thread.getDefaultUncaughtExceptionHandler()))
    }

    fun toast(message: String) {
        object : Thread() {
            override fun run() {
                Looper.prepare()
                Toast.makeText(this@BaseApplication, message, Toast.LENGTH_LONG).show()
                Looper.loop()
            }
        }.start()
    }

    class ExceptionHandler(
        val app: BaseApplication,
        val logger: AndroidLogger,
        val handler: Thread.UncaughtExceptionHandler?
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            app.toast("应用发生错误，错误类型：" + throwable.javaClass)

            val stringWriter = StringWriter()

            throwable.printStackTrace(PrintWriter(stringWriter))

            val str = stringWriter.toString()
            val hash = CodeUtils.md5(str)

            val f = File(logger.LogFile, "$hash.log")
            val hasRaise = f.exists()
            if(hasRaise) {
                logger.debugLog("${DateUtils.currentTime()} 发生异常: $hash")
            } else {
                logger.debugLog("${DateUtils.currentTime()} 发生异常: $hash\n ${throwable.javaClass.name} : ${throwable.message}")
                FileUtils.writeFile(f.toString(), str)
            }

            handler?.uncaughtException(thread, throwable)
        }
    }
}