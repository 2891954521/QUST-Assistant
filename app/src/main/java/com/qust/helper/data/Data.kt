package com.qust.helper.data

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qust.helper.R
import com.qust.helper.ui.page.DailyLesson
import com.qust.helper.ui.page.DrinkPage
import com.qust.helper.ui.page.LoginPage
import com.qust.helper.ui.page.TermLesson
import com.qust.helper.ui.page.eas.GetAcademic
import com.qust.helper.ui.page.eas.GetExams
import com.qust.helper.ui.page.eas.GetLessonTable
import com.qust.helper.ui.page.eas.GetMarks
import com.qust.helper.ui.page.eas.GetNotice

object Data {

	val Pages = mapOf(
		"dailyLesson"   to Page("dailyLesson",  "当日课表",     iconRes = R.drawable.ic_school)     { DailyLesson.DailyLesson(activity = it) },

		"termLesson"    to Page("termLesson",   "学期课表",     iconRes = R.drawable.ic_school)     { TermLesson.TermLesson(activity = it) },

		"easLogin"      to Page("easLogin",     "教务登陆",     iconRes = R.drawable.ic_login)      { LoginPage.EASLoginPage(activity = it) },
		"vpnLogin"      to Page("vpnLogin",     "智慧青科大登陆", iconRes = R.drawable.ic_login)    { LoginPage.IPassLoginPage(activity = it) },

		"easNotice"     to Page("easNotice",    "教务通知",     iconRes = R.drawable.ic_notification) { GetNotice.GetNotice(activity = it) },

		"getLesson"     to Page("getLesson",    "课表查询",     iconRes = R.drawable.ic_school)     { GetLessonTable.GetLessonTable(activity = it) },
		"getMarks"      to Page("getMarks",     "成绩查询",     iconRes = R.drawable.ic_school)     { GetMarks.GetMarks(activity = it) },
		"getAcademic"   to Page("getAcademic",  "学业查询",     iconRes = R.drawable.ic_school)     { GetAcademic.GetAcademic(activity = it) },
		"getExams"      to Page("getExams",     "考试查询",     iconRes = R.drawable.ic_school)     { GetExams.GetExams(activity = it) },

		"drinkCode"     to Page("drinkCode",    "饮水码",       iconRes = R.drawable.ic_water)      { DrinkPage.DrinkPage(activity = it) },

//		"electricRecharge"  to Page("electricRecharge", "电费充值", iconRes = R.drawable.ic_electric) {  },

//		"setting"       to Page("setting",      "设置",         image = Icons.Rounded.Settings)     {  },
	)

	val TermName = arrayOf(
		"大一 上学期", "大一 下学期",
		"大二 上学期", "大二 下学期",
		"大三 上学期", "大三 下学期",
		"大四 上学期", "大四 下学期"
	)

	val WEEK_STRING = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

	/**
	 * 课程时间表
	 */
	val LESSON_TIME_TEXT = arrayOf(
		arrayOf(
			arrayOf("08:00", "09:00", "10:10", "11:10", "13:30", "14:30", "15:40", "16:40", "18:00", "19:00"),
			arrayOf("08:50", "09:50", "11:00", "12:00", "14:20", "15:20", "16:30", "17:30", "18:50", "19:50")
		),
		arrayOf(
			arrayOf("08:00", "09:00", "10:10", "11:10", "14:00", "15:00", "16:10", "17:10", "18:30", "19:30"),
			arrayOf("08:50", "09:50", "11:00", "12:00", "14:50", "15:50", "17:00", "18:00", "19:20", "20:20")
		)
	)

	/**
	 * 课程时间差 (单位：分钟)
	 */
	val LESSON_TIME = arrayOf(
		intArrayOf(0, 60, 70, 60, 140, 60, 70, 60, 80, 60),
		intArrayOf(0, 60, 70, 60, 170, 60, 70, 60, 80, 60)
	)


	class Page(
		val key: String,
		val name: String,
		val iconRes: Int = 0,
		val image: ImageVector? = null,
		val content: @Composable (ComponentActivity) -> Unit = { }
	)
}

