package com.qust.helper.next.entity.lesson

import androidx.compose.runtime.Immutable

/**
 * 全部时间表配置与当前选中项
 *
 * 纯数据，不含持久化；内置配置固定排在用户配置之前，且不参与持久化。
 */
@Immutable
data class TimeTableCatalog(
	val activeId: String = TimeTablePresets.WINTER_NEW_ID,
	val userConfigs: List<TimeTableConfig> = emptyList(),
) {

	/** 内置在前，用户配置在后 */
	val configs: List<TimeTableConfig> get() = TimeTablePresets.BUILT_IN + userConfigs

	fun find(id: String?): TimeTableConfig? = configs.firstOrNull { it.id == id }

	fun isBuiltIn(id: String?): Boolean = TimeTablePresets.builtIn(id) != null

	/** 当前生效的配置；[activeId] 无效时回退默认冬季（[TimeTablePresets.WINTER_NEW]） */
	fun active(): TimeTableConfig = find(activeId) ?: TimeTablePresets.WINTER_NEW

	fun currentTimeTable(): TimeTable = active().timeTable

	fun canAdd(): Boolean = userConfigs.size < MAX_CONFIGS

	/** 选中一份配置；id 无效时返回自身 */
	fun select(id: String): TimeTableCatalog = if(find(id) == null) this else copy(activeId = id)

	/**
	 * 新建一份用户配置
	 * @return null 表示被拒绝（超上限 / id 非法或重复 / 名称为空）
	 */
	fun create(id: String, name: String, timeTable: TimeTable, activate: Boolean = true): TimeTableCatalog? {
		if(!canAdd() || id.isBlank() || name.isBlank()) return null
		if(configs.any { it.id == id }) return null

		val config = TimeTableConfig(
			id = id,
			name = name,
			startTime = timeTable.startTime.toList(),
			endTime = timeTable.endTime.toList(),
		)
		return copy(
			activeId = if(activate) id else activeId,
			userConfigs = userConfigs + config,
		)
	}

	/** 复制任意一份配置（内置也可）为新配置 */
	fun copy(id: String, newId: String, name: String, activate: Boolean = true): TimeTableCatalog? {
		val source = find(id) ?: return null
		return create(newId, name, source.timeTable, activate)
	}

	/** 改名；内置或不存在 → null */
	fun rename(id: String, name: String): TimeTableCatalog? {
		if(name.isBlank() || isBuiltIn(id) || userConfigs.none { it.id == id }) return null
		return copy(userConfigs = userConfigs.map { if(it.id == id) it.copy(name = name) else it })
	}

	/** 修改节次时间；内置或不存在 → null */
	fun update(id: String, timeTable: TimeTable): TimeTableCatalog? {
		if(isBuiltIn(id) || userConfigs.none { it.id == id }) return null
		return copy(userConfigs = userConfigs.map {
			if(it.id == id) {
				it.copy(startTime = timeTable.startTime.toList(), endTime = timeTable.endTime.toList())
			}else {
				it
			}
		})
	}

	/** 删除；内置或不存在 → null。删除当前选中项时回退默认冬季 */
	fun delete(id: String): TimeTableCatalog? {
		if(isBuiltIn(id) || userConfigs.none { it.id == id }) return null
		return copy(
			activeId = if(activeId == id) TimeTablePresets.WINTER_NEW_ID else activeId,
			userConfigs = userConfigs.filterNot { it.id == id },
		)
	}

	companion object {
		/** 用户配置数量上限 */
		const val MAX_CONFIGS = 20
	}
}
