package com.qust.helper.next.repository

import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.SettingKeys
import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.entity.lesson.TimeTableCatalog
import com.qust.helper.next.entity.lesson.TimeTableConfig
import com.qust.helper.next.entity.lesson.TimeTableStoreCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock

/**
 * 时间表配置仓库
 *
 * 维护多份时间表配置与当前选中项，并在变更后驱动课表使用的时间表。
 *
 * 注意：本对象的初始化阶段不得访问 [LessonTableRepository]，否则会与
 * [LessonTableRepository] 的初始化相互触发（Kotlin object 初始化循环）；
 * 只有下面的动作方法才会通知课表仓库。
 */
object TimeTableRepository {

	private val _catalog = MutableStateFlow(
		TimeTableStoreCodec.decode(AppSetting.getString(SettingKeys.SETTING_TIME_TABLES, ""))
			?: TimeTableStoreCodec.seed()
	)

	val catalog: StateFlow<TimeTableCatalog> = _catalog

	private val _currentTimeTable = MutableStateFlow(_catalog.value.currentTimeTable())

	/** 当前生效的时间表 */
	val currentTimeTable: StateFlow<TimeTable> = _currentTimeTable

	fun active(): TimeTableConfig = _catalog.value.active()

	fun canAdd(): Boolean = _catalog.value.canAdd()

	/** 切换到指定配置（内置或用户配置均可） */
	fun switchTo(id: String) {
		commit(_catalog.value.select(id))
	}

	/**
	 * 新建一份配置
	 * @return 新配置 id；被拒绝（超上限 / 名称为空）时返回 null
	 */
	fun createConfig(name: String, timeTable: TimeTable, activate: Boolean = true): String? {
		val id = newConfigId()
		val next = _catalog.value.create(id, name.trim(), timeTable, activate) ?: return null
		commit(next)
		return id
	}

	/**
	 * 复制一份配置（内置也可）
	 * @return 新配置 id；被拒绝时返回 null
	 */
	fun copyConfig(id: String, name: String, activate: Boolean = true): String? {
		val newId = newConfigId()
		val next = _catalog.value.copy(id, newId, name.trim(), activate) ?: return null
		commit(next)
		return newId
	}

	/** 改名；内置配置或不存在时返回 false */
	fun renameConfig(id: String, name: String): Boolean {
		val next = _catalog.value.rename(id, name.trim()) ?: return false
		commit(next)
		return true
	}

	/** 修改节次时间；内置配置或不存在时返回 false */
	fun updateConfig(id: String, timeTable: TimeTable): Boolean {
		val next = _catalog.value.update(id, timeTable) ?: return false
		commit(next)
		return true
	}

	/**
	 * 删除配置；内置配置或不存在时返回 false
	 *
	 * 删除当前选中项时，[TimeTableCatalog.delete] 会把选中项回退到冬季。
	 */
	fun deleteConfig(id: String): Boolean {
		val next = _catalog.value.delete(id) ?: return false
		commit(next)
		return true
	}

	/** 生成不与现有配置冲突的 id */
	fun newConfigId(): String {
		val base = "t${Clock.System.now().toEpochMilliseconds()}"
		val used = _catalog.value.configs.mapTo(HashSet()) { it.id }
		if(base !in used) return base

		var suffix = 1
		var id = base
		while(id in used) {
			suffix++
			id = "$base-$suffix"
		}
		return id
	}

	private fun commit(next: TimeTableCatalog) {
		if(next == _catalog.value) return

		_catalog.value = next
		AppSetting[SettingKeys.SETTING_TIME_TABLES] = TimeTableStoreCodec.encode(next)

		val timeTable = next.currentTimeTable()
		_currentTimeTable.value = timeTable
		LessonTableRepository.applyTimeTable(timeTable)
	}
}
