package com.qust.helper.next.entity

object SettingKeys {

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
	 * 时间表配置集合（JSON：用户配置 + 当前选中项）
	 */
	const val SETTING_TIME_TABLES = "setting_time_tables"

	/**
	 * 已保存课程所对应的时间表，用于「按新时间表对齐课程」
	 */
	const val SETTING_LESSON_TIME_TABLE = "setting_lesson_time_table"

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


	/** 课表设置 */
	const val SETTING_TOTAL_WEEK = "setting_total_week"
	const val SETTING_START_DAY = "setting_start_day"
	const val SETTING_LESSON_SHOW_NON_CURRENT_WEEK = "setting_lesson_show_non_current_week"
	const val SETTING_LESSON_SHOW_FINISHED = "setting_lesson_show_finished"
	const val SETTING_LESSON_HIDE_TEACHER = "setting_lesson_hide_teacher"


	/**
	 * 使用百分比布局
	 */
	const val SETTING_UI_PERCENTAGE_LAYOUT = "setting_ui_percentage_layout"

	/**
	 * UI缩放
	 */
	const val SETTING_UI_UI_SCALE = "setting_ui_ui_scale"

	/**
	 * 字体缩放
	 */
	const val SETTING_UI_FONT_SCALE = "setting_ui_font_scale"
}