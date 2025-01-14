package com.qust.helper.data

import com.qust.helper.R
import com.qust.helper.ui.page.DrinkPage
import com.qust.helper.ui.page.MyPage
import com.qust.helper.ui.page.account.AccountManagerPage
import com.qust.helper.ui.page.account.LoginPage
import com.qust.helper.ui.page.app.AppPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.app.UpdatePage
import com.qust.helper.ui.page.business.ElectricRecharge
import com.qust.helper.ui.page.business.SportTest
import com.qust.helper.ui.page.eas.GetAcademic
import com.qust.helper.ui.page.eas.GetExams
import com.qust.helper.ui.page.eas.GetLessonTable
import com.qust.helper.ui.page.eas.GetMarks
import com.qust.helper.ui.page.eas.GetNotice
import com.qust.helper.ui.page.lesson.DailyLesson
import com.qust.helper.ui.page.lesson.TermLesson
import com.qust.helper.ui.page.web.WebPage

object Data {

	val pageGroups = arrayOf(
		DrawerPageGroup.Page(Keys.Page.DailyLessonPage),
		DrawerPageGroup.Page(Keys.Page.TermLessonPage),

		DrawerPageGroup.Page(Keys.Page.EasWebPage),

		DrawerPageGroup.PageGroup(name = "教务查询", iconRes = R.drawable.ic_school, pages = arrayOf(
			Keys.Page.GetNoticePage,
			Keys.Page.GetLessonPage,
			Keys.Page.GetMarksPage,
			Keys.Page.GetAcademicPage,
			Keys.Page.GetExamsPage
		)),

		DrawerPageGroup.Page(Keys.Page.IpassWebPage),

		DrawerPageGroup.Page(Keys.Page.ElectricPage),

		DrawerPageGroup.Page(Keys.Page.DrinkPage),

		DrawerPageGroup.PageGroup(name = "账号登录", iconRes = R.drawable.ic_login, pages = arrayOf(
			Keys.Page.EasLogin, Keys.Page.VpnLoginPage
		)),

		DrawerPageGroup.Page(Keys.Page.SettingPage)
	)

	val Pages = mapOf(
		Keys.Page.MyPage to MyPage.MyPage,

		Keys.Page.DailyLessonPage to DailyLesson.DailyLessonPage,
		Keys.Page.TermLessonPage to TermLesson.TermLessonPage,


		Keys.Page.GetNoticePage to GetNotice.GetNoticePage,
		Keys.Page.GetLessonPage to GetLessonTable.GetLessonPage,
		Keys.Page.GetMarksPage to GetMarks.GetMarksPage,
		Keys.Page.GetAcademicPage to GetAcademic.GetAcademicPage,
		Keys.Page.GetExamsPage to GetExams.GetExamsPage,

		Keys.Page.ElectricPage to ElectricRecharge.ElectricPage,
		Keys.Page.SportTestPage to SportTest.SportTestPage,

		Keys.Page.DrinkPage to DrinkPage.DrinkPage,

		Keys.Page.EasWebPage to WebPage.EasWebPage,
		Keys.Page.IpassWebPage to WebPage.IpassWebPage,

		Keys.Page.AccountManager to AccountManagerPage.AccountManagerPage,
		Keys.Page.EasLogin to LoginPage.EasLoginPage,
		Keys.Page.VpnLoginPage to LoginPage.VpnLoginPage,

		Keys.Page.SettingPage to SettingPage.SettingPage,
		Keys.Page.UpdatePage to UpdatePage.UpdatePage,

		Keys.Page.UserAgreementPage to AppPage.UserAgreementPage,
		Keys.Page.PolicyPage to AppPage.PolicyPage,
	)

	val bottomPage = arrayOf(
		TermLesson.TermLessonPage,
		MyPage.MyPage,
	)

	val TermName = arrayOf(
		"大一 上学期", "大一 下学期",
		"大二 上学期", "大二 下学期",
		"大三 上学期", "大三 下学期",
		"大四 上学期", "大四 下学期"
	)

	val WEEK_STRING = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

	/**
	 * 课程时间差 (单位：分钟)
	 */
	val LESSON_TIME = arrayOf(
		intArrayOf(0, 60, 70, 60, 140, 60, 70, 60, 110, 60),
		intArrayOf(0, 60, 70, 60, 170, 60, 70, 60, 110, 60)
	)
}

