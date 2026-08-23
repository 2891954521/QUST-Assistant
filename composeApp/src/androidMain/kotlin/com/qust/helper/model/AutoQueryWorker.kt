package com.qust.helper.model

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.LessonQuery
import com.qust.helper.repository.LessonTableRepository
import com.qust.helper.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 后台自动查询课表 Worker
 */
class AutoQueryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		return try {
			withContext(Dispatchers.IO) {
				try {
					val account = EasAccount.getAccountName()
					if(account.length >= 4) {
						// 参照 GuideActivity 登录成功后的逻辑计算年级、学年学期并查询保存课表
						val year = Calendar.getInstance().get(Calendar.YEAR).toString()
						val entranceTime = (year.substring(0, year.length - 2) + account.substring(0, 2)).toInt()
						EasAccount.entranceDate = entranceTime
						val grade = EasAccount.getCurrentGrade()
						val result = LessonQuery.queryLessonTable(
							EasAccount,
							(grade / 2 + entranceTime).toString(),
							if(grade % 2 == 0) "3" else "12"
						)
						if(result.lessons != null) {
							LessonTableRepository.saveLessonTable(result)
						}
					}
				} catch(e: Exception) {
					Logger.e(e = e)
				}
			}
			Result.success()
		} catch(throwable: Throwable) {
			Logger.e(e = throwable)
			Result.failure()
		}
	}
}
