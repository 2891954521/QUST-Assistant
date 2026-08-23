package com.qust.helper.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 自动查询工具类，通过 WorkManager 每 12 小时在后台自动查询并保存课表
 */
object AutoQueryRepository {

	private const val UNIQUE_WORK_NAME = "AutoQuery"

	/**
	 * 开始自动查询
	 */
	fun startAutoQuery(context: Context) {
		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
			UNIQUE_WORK_NAME,
			ExistingPeriodicWorkPolicy.KEEP,
			PeriodicWorkRequestBuilder<AutoQueryWorker>(12, TimeUnit.HOURS)
				.setConstraints(
					Constraints.Builder()
						.setRequiredNetworkType(NetworkType.CONNECTED)
						.setRequiresBatteryNotLow(true)
						.build()
				)
				.build()
		)
	}
}
