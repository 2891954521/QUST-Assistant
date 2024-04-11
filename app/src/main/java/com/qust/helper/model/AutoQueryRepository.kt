package com.qust.helper.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


/**
 * 自动查询工具类
 */
object AutoQueryRepository {

	/**
	 * 开始自动查询
	 */
	fun startAutoQuery(context: Context) {
		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
			"AutoQuery",
			ExistingPeriodicWorkPolicy.KEEP,
			PeriodicWorkRequestBuilder<AutoQueryWorker>(12, TimeUnit.HOURS)
				.setConstraints(
					Constraints.Builder()
					.setRequiredNetworkType(NetworkType.UNMETERED)
					.setRequiresBatteryNotLow(true)
					.build()
			).build()
		)
	}
}
