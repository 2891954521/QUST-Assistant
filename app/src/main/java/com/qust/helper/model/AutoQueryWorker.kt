package com.qust.helper.model

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.NeedLoginException
import com.qust.helper.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AutoQueryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		return try {
			withContext(Dispatchers.IO){
				try {
					val easAccount = EASAccount.getInstance()
					if(easAccount.checkLogin()) {
//						val currentYear = easAccount.getCurrentGrade()
//						val xnm = (currentYear / 2 + easAccount.entranceTime).toString()
//						val xqm = if(currentYear % 2 == 0) "3" else "12"

						val notices = easAccount.queryNotice(1, 1)
						if(notices.isNotEmpty()) NotificationUtils.sendNotification(applicationContext, "教务通知", notices.first().content)

//						val mark = queryMark(activity, easAccount, currentYear, xnm, xqm)
//						if(mark != null) NotificationUtils.sendNotification(activity, "成绩通知", mark)
//
//						val exam = queryExam(activity, easAccount, currentYear, xnm, xqm)
//						if(exam != null) NotificationUtils.sendNotification(activity, "考试通知", exam)
					}
				} catch(ignore: NeedLoginException) {
				} catch(ignore: IOException) {
				}
			}
			Result.success()
		} catch (throwable: Throwable) {
			Result.failure()
		}
	}

}