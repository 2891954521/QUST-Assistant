package com.qust.helper.next.entity.lesson

import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.entity.lesson.TimeTableStoreCodec.seed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 时间表配置的持久化编解码
 *
 * 只持久化用户配置与当前选中项，内置配置不入库（内置由代码常量提供）。
 */
object TimeTableStoreCodec {

	const val VERSION = 1

	private val json = Json {
		ignoreUnknownKeys = true
		encodeDefaults = true
	}

	@Serializable
	private data class ConfigDto(
		val id: String,
		val name: String,
		val start: List<Int> = emptyList(),
		val end: List<Int> = emptyList(),
	)

	@Serializable
	private data class StoreDto(
		val version: Int = VERSION,
		val activeId: String = TimeTablePresets.WINTER_NEW_ID,
		val configs: List<ConfigDto> = emptyList(),
	)

	@Serializable
	private data class TimeTableDto(
		val start: List<Int> = emptyList(),
		val end: List<Int> = emptyList(),
	)

	/** 首次使用时的空配置：只有内置配置，选中默认冬季（[TimeTablePresets.WINTER_NEW]） */
	fun seed(): TimeTableCatalog = TimeTableCatalog()

	fun encode(catalog: TimeTableCatalog): String = json.encodeToString(
		StoreDto.serializer(),
		StoreDto(
			activeId = catalog.activeId,
			configs = catalog.userConfigs.map { ConfigDto(it.id, it.name, it.startTime, it.endTime) },
		)
	)

	/**
	 * 解码配置集合
	 * @return null 表示存储值缺失或整体损坏，调用方应回退到 [seed]
	 */
	fun decode(value: String?): TimeTableCatalog? {
		if(value.isNullOrBlank()) return null

		val store = try {
			json.decodeFromString(StoreDto.serializer(), value)
		}catch(e: Exception) {
			Logger.e("TimeTableStoreCodec.decode", e)
			return null
		}

		if(store.version != VERSION) {
			Logger.w("Time table store version ${store.version} != $VERSION")
		}

		val configs = store.configs
			.take(TimeTableCatalog.MAX_CONFIGS)
			.mapNotNull { it.toConfig() }

		return TimeTableCatalog(activeId = store.activeId, userConfigs = configs)
	}

	/** 编码单份时间表（课表课程所对应的时间表） */
	fun encodeTimeTable(timeTable: TimeTable): String = json.encodeToString(
		TimeTableDto.serializer(),
		TimeTableDto(start = timeTable.startTime.toList(), end = timeTable.endTime.toList())
	)

	/** @return null 表示缺失或损坏 */
	fun decodeTimeTable(value: String?): TimeTable? {
		if(value.isNullOrBlank()) return null

		val dto = try {
			json.decodeFromString(TimeTableDto.serializer(), value)
		}catch(e: Exception) {
			Logger.e("TimeTableStoreCodec.decodeTimeTable", e)
			return null
		}
		if(TimeTableValidator.validate(dto.start, dto.end) != null) return null

		return TimeTable(dto.start.toTypedArray(), dto.end.toTypedArray())
	}

	private fun ConfigDto.toConfig(): TimeTableConfig? {
		if(id.isBlank() || TimeTablePresets.builtIn(id) != null) {
			Logger.w("Drop time table config with blank or reserved id '$id'")
			return null
		}
		val issue = TimeTableValidator.validate(start, end)
		if(issue != null) {
			Logger.w("Drop invalid time table config '$id': ${issue.message}")
			return null
		}
		return TimeTableConfig(id = id, name = name.ifBlank { id }, startTime = start, endTime = end)
	}
}
