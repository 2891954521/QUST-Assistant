package com.qust.helper

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.qust.helper.data.Setting
import com.qust.helper.model.Logger

class App: Application() {

	override fun onCreate() {
		super.onCreate()

		Setting.init(this)
		Logger.init(this)

		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler()))

		// 初始化滑动返回框架
		// SmartSwipeBack.activitySlidingBack(this, activitySwipeBackFilter, 0, -0x80000000, 0, 0, 0.5f, SwipeConsumer.DIRECTION_LEFT)
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

	// private val activitySwipeBackFilter: SmartSwipeBack.ActivitySwipeBackFilter = SmartSwipeBack.ActivitySwipeBackFilter { activity -> !(activity is MainActivity || activity is GuideActivity) }
}

private class ExceptionHandler(
	val app: App,
	val handler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

	override fun uncaughtException(thread: Thread, throwable: Throwable) {
		app.toast("应用发生错误，错误类型：" + throwable.javaClass)
//		LogUtil.Log("-------应用异常退出-------", throwable)
//		LogUtil.debugLog("-------应用异常退出-------\n")
		handler?.uncaughtException(thread, throwable)
	}
}