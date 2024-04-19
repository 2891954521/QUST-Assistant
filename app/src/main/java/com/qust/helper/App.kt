package com.qust.helper

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.model.Logger
import com.umeng.commonsdk.UMConfigure

class App: Application() {

	override fun onCreate() {
		super.onCreate()

		Setting.init(this)

		if(BuildConfig.UMENG_APP_KEY.isNotEmpty()){
			UMConfigure.setLogEnabled(false)
			UMConfigure.preInit(this, BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_APP_CHANNEL)
			if(!Setting.getBoolean(Keys.IS_FIRST_USE, true)) {
				UMConfigure.init(this, BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_APP_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "")
			}
		}

		Logger.init(this)

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