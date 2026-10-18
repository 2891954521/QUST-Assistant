package com.qust.helper.next.entity.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTableStoreCodecTest {

	@Test
	fun roundTripKeepsUserConfigsAndActive() {
		val custom = table(8_00 to 8_45)
		val catalog = TimeTableCatalog()
			.create("t1", "配置一", custom)!!
			.create("t2", "配置二", table(9_00 to 9_45), activate = false)!!
			.select("t2")

		val decoded = TimeTableStoreCodec.decode(TimeTableStoreCodec.encode(catalog))!!
		assertEquals("t2", decoded.activeId)
		assertEquals(listOf("t1", "t2"), decoded.userConfigs.map { it.id })
		assertEquals(listOf("配置一", "配置二"), decoded.userConfigs.map { it.name })
		assertEquals(custom, decoded.find("t1")!!.timeTable)
		assertEquals(TimeTablePresets.BUILT_IN.size + 2, decoded.configs.size)
	}

	@Test
	fun builtInIsNotPersisted() {
		val decoded = TimeTableStoreCodec.decode(TimeTableStoreCodec.encode(TimeTableCatalog()))!!
		assertTrue(decoded.userConfigs.isEmpty())
		assertEquals(TimeTablePresets.BUILT_IN.size, decoded.configs.size)
	}

	@Test
	fun seedHasOnlyBuiltIn() {
		val seed = TimeTableStoreCodec.seed()
		assertEquals(TimeTablePresets.WINTER_NEW_ID, seed.activeId)
		assertTrue(seed.userConfigs.isEmpty())
	}

	@Test
	fun decodeInvalidReturnsNull() {
		assertNull(TimeTableStoreCodec.decode(null))
		assertNull(TimeTableStoreCodec.decode(""))
		assertNull(TimeTableStoreCodec.decode("   "))
		assertNull(TimeTableStoreCodec.decode("not json"))
		assertNull(TimeTableStoreCodec.decode("""{"version":1,"activeId":"winter","configs":"x"}"""))
	}

	@Test
	fun decodeDropsInvalidAndReservedEntries() {
		val raw = """
			{"version":1,"activeId":"t1","configs":[
			  {"id":"t1","name":"正常的","start":[800,900],"end":[845,945]},
			  {"id":"t2","name":"起止颠倒","start":[900],"end":[845]},
			  {"id":"winter","name":"与内置冲突","start":[800],"end":[845]},
			  {"id":"","name":"空 id","start":[800],"end":[845]},
			  {"id":"t3","name":"节数不一致","start":[800,900],"end":[845]}
			]}
		""".trimIndent()

		val decoded = TimeTableStoreCodec.decode(raw)!!
		assertEquals(listOf("t1"), decoded.userConfigs.map { it.id })
	}

	@Test
	fun timeTableRoundTrip() {
		val table = TimeTablePresets.WINTER_NEW.timeTable
		assertEquals(table, TimeTableStoreCodec.decodeTimeTable(TimeTableStoreCodec.encodeTimeTable(table)))
	}

	@Test
	fun decodeTimeTableInvalidReturnsNull() {
		assertNull(TimeTableStoreCodec.decodeTimeTable(null))
		assertNull(TimeTableStoreCodec.decodeTimeTable(""))
		assertNull(TimeTableStoreCodec.decodeTimeTable("{}"))
		assertNull(TimeTableStoreCodec.decodeTimeTable("""{"start":[800],"end":[845,945]}"""))
	}

	private fun table(vararg pairs: Pair<Int, Int>) = TimeTable(
		pairs.map { it.first }.toTypedArray(),
		pairs.map { it.second }.toTypedArray(),
	)
}
