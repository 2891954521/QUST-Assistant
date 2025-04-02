package com.qust.helper

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.qust.helper.model.Logger
import com.qust.helper.utils.UmengUtils
import com.tencent.mmkv.MMKV


class App: Application() {

	override fun onCreate() {
		super.onCreate()

		MMKV.initialize(this)

		Logger.init(this)

		if(!BuildConfig.DEBUG) UmengUtils.init(this)

		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler()))
	}

	fun toast(message: String) {
		object : Thread() {
			override fun run() {
				Looper.prepare()
				Toast.makeText(this@App, message, Toast.LENGTH_LONG).show()
				Looper.loop()
			}
		}.start()
	}
}

private class ExceptionHandler(val app: App, val handler: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {
	override fun uncaughtException(thread: Thread, throwable: Throwable) {
		app.toast("应用发生错误，错误类型：" + throwable.javaClass)
//		LogUtil.Log("-------应用异常退出-------", throwable)
//		LogUtil.debugLog("-------应用异常退出-------\n")
		handler?.uncaughtException(thread, throwable)
	}
}