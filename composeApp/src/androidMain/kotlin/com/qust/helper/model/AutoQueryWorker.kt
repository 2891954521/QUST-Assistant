package com.qust.helper.model

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qust.helper.data.Keys
import com.qust.helper.data.room.LessonDatabase
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.NeedLoginException
import com.qust.helper.utils.NotificationUtils
import com.qust.helper.utils.SettingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AutoQueryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		return try {
			withContext(Dispatchers.IO){
				try {
					val lessonData = LessonDatabase.getInstance(applicationContext)

					val easAccount = EASAccount.getInstance()
					if(easAccount.checkLogin()) {

						val notices = easAccount.queryNotice(1, 1)
						if(notices.isNotEmpty()){
							val notice = notices.first()
							val id = SettingUtils[Keys.LAST_NOTICE_ID, ""]
							if(id != notice.id){
								SettingUtils.putString(Keys.LAST_NOTICE_ID, notice.id)
								NotificationUtils.sendNotification(applicationContext, "教务通知", notices.first().content)
							}
						}

						val currentYear = easAccount.getCurrentGrade()
						val xnm = (currentYear / 2 + easAccount.entranceTime).toString()
						val xqm = if(currentYear % 2 == 0) "3" else "12"

						val newMarks = easAccount.queryMark(currentYear, xnm, xqm)
						if(newMarks.isNotEmpty() && newMarks.size > lessonData.markDao().countByIndex(currentYear)) {
							NotificationUtils.sendNotification(applicationContext, "成绩通知", "已查询到新的成绩！")
						}

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