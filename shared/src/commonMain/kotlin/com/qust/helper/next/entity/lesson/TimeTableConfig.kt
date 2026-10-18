package com.qust.helper.next.entity.lesson

import androidx.compose.runtime.Immutable

/**
 * 一份时间表配置
 *
 * @param id 稳定标识（内置配置用固定 id，用户配置由仓库生成）
 * @param name 展示名称
 * @param startTime 每节上课时间，HHMM（如 800 表示 08:00）
 * @param endTime 每节下课时间，HHMM
 * @param builtIn 内置配置：不可改名/修改/删除，只能复制
 */
@Immutable
data class TimeTableConfig(
	val id: String,
	val name: String,
	val startTime: List<Int>,
	val endTime: List<Int>,
	val builtIn: Boolean = false,
) {

	/** 构造前必须已通过 [TimeTableValidator] 校验（两个数组长度一致） */
	val timeTable: TimeTable = TimeTable(startTime.toTypedArray(), endTime.toTypedArray())

	val count: Int get() = startTime.size
}

/**
 * 内置时间表配置
 *
 * 代码常量、不参与持久化，因此应用升级可以修正内置时间。
 */
object TimeTablePresets {

	const val WINTER_NEW_ID = "winter"

	const val WINTER_OLD_ID = "winter_old"
	const val SUMMER_OLD_ID = "summer"

	const val GAOMI_WINTER_ID = "gaomi_winter"
	const val GAOMI_SUMMER_ID = "gaomi_summer"


	/** 冬季，11 节作息（晚课 3 节）：默认时间表 */
	val WINTER_NEW: TimeTableConfig = TimeTableConfig(
		id = WINTER_NEW_ID,
		name = "冬季时间表（新）",
		startTime = listOf(8_00, 8_55, 10_00, 10_55, 13_30, 14_25, 15_30, 16_25, 18_00, 18_55, 19_50),
		endTime = listOf(8_45, 9_40, 10_45, 11_40, 14_15, 15_10, 16_15, 17_10, 18_45, 19_40, 20_35),
		builtIn = true,
	)

	/** 冬季（旧版 10 节作息，仅供沿用） */
	val WINTER_OLD: TimeTableConfig = TimeTableConfig(
		id = WINTER_OLD_ID,
		name = "冬季时间表（旧）",
		startTime = listOf(8_00, 9_00, 10_10, 11_10, 13_30, 14_30, 15_40, 16_40, 18_30, 19_30),
		endTime = listOf(8_50, 9_50, 11_00, 12_00, 14_20, 15_20, 16_30, 17_30, 19_20, 20_20),
		builtIn = true,
	)

	/** 夏季（旧版，第 5 节起推迟到 14:00） */
	val SUMMER_OLD: TimeTableConfig = TimeTableConfig(
		id = SUMMER_OLD_ID,
		name = "夏季时间表（旧）",
		startTime = listOf(8_00, 9_00, 10_10, 11_10, 14_00, 15_00, 16_10, 17_10, 19_00, 20_00),
		endTime = listOf(8_50, 9_50, 11_00, 12_00, 14_50, 15_50, 17_00, 18_00, 19_50, 20_50),
		builtIn = true,
	)

	/** 高密校区冬季（第 1 节 08:30 上课） */
	val GAOMI_WINTER: TimeTableConfig = TimeTableConfig(
		id = GAOMI_WINTER_ID,
		name = "高密校区冬季时间表",
		startTime = listOf(8_30, 9_20, 10_25, 11_15, 13_30, 14_20, 15_25, 16_15, 18_00, 18_50),
		endTime = listOf(9_15, 10_05, 11_10, 12_00, 14_15, 15_05, 16_10, 17_00, 18_45, 19_35),
		builtIn = true,
	)

	/** 高密校区夏季（第 5 节起推迟到 14:00） */
	val GAOMI_SUMMER: TimeTableConfig = TimeTableConfig(
		id = GAOMI_SUMMER_ID,
		name = "高密校区夏季时间表",
		startTime = listOf(8_30, 9_20, 10_25, 11_15, 14_00, 14_50, 15_55, 16_45, 18_30, 19_20),
		endTime = listOf(9_15, 10_05, 11_10, 12_00, 14_45, 15_35, 16_40, 17_30, 19_15, 20_05),
		builtIn = true,
	)

	val BUILT_IN: List<TimeTableConfig> = listOf(WINTER_NEW, WINTER_OLD, SUMMER_OLD, GAOMI_WINTER, GAOMI_SUMMER)

	fun builtIn(id: String?): TimeTableConfig? = BUILT_IN.firstOrNull { it.id == id }
}
