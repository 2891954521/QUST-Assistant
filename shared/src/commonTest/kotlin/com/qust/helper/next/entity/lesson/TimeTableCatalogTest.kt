package com.qust.helper.next.entity.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTableCatalogTest {

	private val custom = table(8_00 to 8_45, 9_00 to 9_45)

	@Test
	fun builtInComesFirst() {
		val catalog = TimeTableCatalog().create("t1", "我的", custom)!!
		assertEquals(TimeTablePresets.BUILT_IN.map { it.id } + "t1", catalog.configs.map { it.id })
		assertTrue(catalog.configs.dropLast(1).all { it.builtIn })
	}

	@Test
	fun createActivatesByDefault() {
		val catalog = TimeTableCatalog().create("t1", "我的", custom)!!
		assertEquals("t1", catalog.activeId)
		assertEquals(custom, catalog.currentTimeTable())
		assertEquals(custom, catalog.active().timeTable)
	}

	@Test
	fun createCanStayInactive() {
		val catalog = TimeTableCatalog().create("t1", "我的", custom, activate = false)!!
		assertEquals(TimeTablePresets.WINTER_NEW_ID, catalog.activeId)
	}

	@Test
	fun createRejectsBlankNameAndDuplicateId() {
		val catalog = TimeTableCatalog()
		assertNull(catalog.create("", "名称", custom))
		assertNull(catalog.create("t1", "   ", custom))

		val created = catalog.create("t1", "名称", custom)!!
		assertNull(created.create("t1", "另一个", custom))
		assertNull(created.copy("winter", "t1", "副本"))
	}

	@Test
	fun createRejectsOverLimit() {
		var catalog = TimeTableCatalog()
		repeat(TimeTableCatalog.MAX_CONFIGS) { i ->
			catalog = catalog.create("t$i", "配置 $i", custom)!!
		}
		assertTrue(!catalog.canAdd())
		assertNull(catalog.create("over", "超限", custom))
	}

	@Test
	fun copyFromBuiltIn() {
		val catalog = TimeTableCatalog().copy("summer", "s1", "夏季副本")!!
		assertEquals("s1", catalog.activeId)
		assertEquals(TimeTablePresets.SUMMER_OLD.timeTable, catalog.find("s1")!!.timeTable)
		assertTrue(!catalog.find("s1")!!.builtIn)
		assertNull(catalog.copy("nope", "s2", "副本"))
	}

	@Test
	fun selectIgnoresUnknownId() {
		val catalog = TimeTableCatalog()
		assertEquals(TimeTablePresets.WINTER_NEW_ID, catalog.select("nope").activeId)
		assertEquals(TimeTablePresets.SUMMER_OLD_ID, catalog.select(TimeTablePresets.SUMMER_OLD_ID).activeId)
	}

	@Test
	fun activeFallsBackToWinter() {
		val catalog = TimeTableCatalog(activeId = "gone")
		assertEquals(TimeTablePresets.WINTER_NEW_ID, catalog.active().id)
		assertEquals(TimeTablePresets.WINTER_NEW.timeTable, catalog.currentTimeTable())
	}

	@Test
	fun builtInIsReadOnly() {
		val catalog = TimeTableCatalog()
		assertNull(catalog.rename(TimeTablePresets.WINTER_NEW_ID, "改名"))
		assertNull(catalog.update(TimeTablePresets.WINTER_NEW_ID, custom))
		assertNull(catalog.delete(TimeTablePresets.WINTER_NEW_ID))
		assertNull(catalog.rename("nope", "改名"))
		assertNull(catalog.delete("nope"))
	}

	@Test
	fun updateAndRenameCustom() {
		val catalog = TimeTableCatalog().create("t1", "我的", custom)!!
		val newTable = table(7_30 to 8_15)

		val updated = catalog.update("t1", newTable)!!
		assertEquals(newTable, updated.find("t1")!!.timeTable)
		// 原 catalog 不可变
		assertEquals(custom, catalog.find("t1")!!.timeTable)
		assertEquals("改名后", updated.rename("t1", "改名后")!!.find("t1")!!.name)
	}

	@Test
	fun deleteActiveFallsBackToWinter() {
		val catalog = TimeTableCatalog().create("t1", "我的", custom)!!
		val deleted = catalog.delete("t1")!!
		assertEquals(TimeTablePresets.WINTER_NEW_ID, deleted.activeId)
		assertTrue(deleted.userConfigs.isEmpty())
	}

	@Test
	fun deleteInactiveKeepsActive() {
		val catalog = TimeTableCatalog()
			.create("t1", "一", custom)!!
			.create("t2", "二", custom, activate = false)!!
			.select("t1")
		val deleted = catalog.delete("t2")!!
		assertEquals("t1", deleted.activeId)
		assertEquals(listOf("t1"), deleted.userConfigs.map { it.id })
	}

	private fun table(vararg pairs: Pair<Int, Int>) = TimeTable(
		pairs.map { it.first }.toTypedArray(),
		pairs.map { it.second }.toTypedArray(),
	)
}
