package com.qust.helper.data

object Keys {

	/**
	 * 是否是第一次使用
	 */
	const val IS_FIRST_USE = "isFirstUse"

	/**
	 * 教务节点
	 */
	const val EA_HOST = "eaHost"

	/**
	 * 使用VPN访问教务
	 */
	const val EA_USE_VPN = "eaUseVpn"

	/**
	 * 入学时间
	 */
	const val ENTRANCE_TIME = "key_entrance_time"

	/**
	 * 显示所有课程
	 */
	const val KEY_SHOW_ALL_LESSON = "key_show_all_lesson"

	/**
	 * 隐藏已结课课程
	 */
	const val KEY_HIDE_FINISH_LESSON = "key_hide_finish_lesson"

	/**
	 * 隐藏教师
	 */
	const val KEY_HIDE_TEACHER = "key_hide_teacher"

	/**
	 * 高密时间表
	 */
	const val KEY_TIME_TABLE = "key_time_table"

	/**
	 * 课表时间表 0：冬季， 1：夏季
	 */
	const val KEY_GAOMI_TIME_TABLE = "key_gaomi_time_table"

	/**
	 * 锁定课表
	 */
	const val KEY_LOCK_LESSON = "key_lock_lesson"


	/**
	 * 主题跟随系统
	 */
	const val KEY_THEME_FOLLOW_SYSTEM = "key_theme_follow_system"
	/**
	 * 暗色模式
	 */
	const val KEY_THEME_DARK = "key_theme_dark"


	/**
	 * 自动检查更新
	 */
	const val KEY_AUTO_UPDATE = "key_auto_update"

	/**
	 * 上次检查更新的时间
	 */
	const val LAST_UPDATE_TIME = "last_update_time"

	/**
	 * 上一个教务公告的ID
	 */
	const val LAST_NOTICE_ID = "last_notice_id"

	const val EAS_ACCOUNT = "eas_account"
	const val EAS_PASSWORD = "eas_password"

	const val IPASS_ACCOUNT = "ipass_account"
	const val IPASS_PASSWORD = "ipass_password"

	const val DRINK_ACCOUNT = "drink_account"
	const val DRINK_PASSWORD = "drink_password"

	object Page {

		const val MyPage = "myPage"

		const val DailyLessonPage = "dailyLessonPage"
		const val TermLessonPage = "termLessonPage"

		const val GetLessonPage = "getLessonPage"
		const val GetMarksPage = "getMarksPage"
		const val GetNoticePage = "getNoticePage"
		const val GetAcademicPage = "getAcademicPage"
		const val GetExamsPage = "getExamsPage"

		const val ElectricPage = "electricPage"
		const val SportTestPage = "SportTestPage"

		const val DrinkPage = "drinkPage"

		const val EasWebPage = "easWebPage"
		const val IpassWebPage = "ipassWebPage"

		const val AccountManager = "accountManager"

		const val EasLogin = "easLogin"
		const val VpnLoginPage = "vpnLoginPage"

		const val SettingPage = "setting"
		const val UpdatePage = "updatePage"

		const val UserAgreementPage = "userAgreementPage"
		const val PolicyPage = "policyPage"

	}
}