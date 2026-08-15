package com.qust.helper.next.entity

open class I18nStrings {

	open val TEXT_OK = "确定"
	open val TEXT_CANCEL = "取消"

	open val TEXT_LOGIN = "登录"

	open val TEXT_TERM = "学期"
	open val TEXT_SAVE_LESSON_TABLE = "保存课表"

	open val TEXT_NEW = "新"


	open val MSG_NEED_LOGIN: String = "需要登录"

	open val MSG_ERROR_ACCOUNT: String = "用户名或密码错误"

	open val MSG_ERROR_LOGIC: String = "逻辑错误"

	open val MSG_QUERY_TERM_START_TIME = "当前开学日期: %s\n查询到的开学日期: %s"


	open val ARRAY_WEEK_NAME = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

	open val ARRAY_TERM_NAME = listOf(
		"大一 上学期", "大一 下学期",
		"大二 上学期", "大二 下学期",
		"大三 上学期", "大三 下学期",
		"大四 上学期", "大四 下学期"
	)

	open val ARRAY_QUERY_LESSON_TYPE = listOf("个人课表", "班级课表")

}

val Strings: I18nStrings = I18nStrings()