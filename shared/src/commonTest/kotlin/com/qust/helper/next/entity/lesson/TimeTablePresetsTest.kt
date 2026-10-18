package com.qust.helper.next.entity.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTablePresetsTest {

	@Test
	fun builtInIdsAreUniqueAndFlagged() {
		val ids = TimeTablePresets.BUILT_IN.map { it.id }
		assertEquals(ids.size, ids.toSet().size)
		assertTrue(TimeTablePresets.BUILT_IN.all { it.builtIn })
	}

	@Test
	fun winterOldKeepsHistoricalTimes() {
		val winterOld = TimeTablePresets.WINTER_OLD
		assertEquals(10, winterOld.count)
		assertEquals(8 * 60, winterOld.timeTable.startMinute.first())
		assertEquals(8 * 60 + 50, winterOld.timeTable.endMinute.first())
		assertEquals(13 * 60 + 30, winterOld.timeTable.startMinute[4])
		assertEquals(14 * 60 + 20, winterOld.timeTable.endMinute[4])
		assertEquals(19 * 60 + 30, winterOld.timeTable.startMinute.last())
		assertEquals(20 * 60 + 20, winterOld.timeTable.endMinute.last())
	}

	@Test
	fun gaomiPresetsKeepTimes() {
		val winter = TimeTablePresets.GAOMI_WINTER
		assertEquals(10, winter.count)
		assertEquals(8 * 60 + 30, winter.timeTable.startMinute.first())
		assertEquals(9 * 60 + 15, winter.timeTable.endMinute.first())
		assertEquals(18 * 60 + 50, winter.timeTable.startMinute.last())
		assertEquals(19 * 60 + 35, winter.timeTable.endMinute.last())

		val summer = TimeTablePresets.GAOMI_SUMMER
		assertEquals(10, summer.count)
		// 上午四节相同，第 5 节起推迟到 14:00
		assertEquals(winter.timeTable.startMinute.take(4), summer.timeTable.startMinute.take(4))
		assertEquals(winter.timeTable.endMinute.take(4), summer.timeTable.endMinute.take(4))
		assertEquals(14 * 60, summer.timeTable.startMinute[4])
		assertEquals(20 * 60 + 5, summer.timeTable.endMinute.last())
	}

	@Test
	fun winterNewIsElevenPeriods() {
		val winterNew = TimeTablePresets.WINTER_NEW
		assertEquals(11, winterNew.count)
		assertEquals(8 * 60, winterNew.timeTable.startMinute.first())
		assertEquals(8 * 60 + 45, winterNew.timeTable.endMinute.first())
		assertEquals(19 * 60 + 50, winterNew.timeTable.startMinute.last())
		assertEquals(20 * 60 + 35, winterNew.timeTable.endMinute.last())
		// 第 9~11 节为晚课
		assertEquals(18 * 60, winterNew.timeTable.startMinute[8])
	}

	@Test
	fun everyPresetIsValid() {
		TimeTablePresets.BUILT_IN.forEach { config ->
			assertNull(TimeTableValidator.validate(config.startTime, config.endTime), "invalid preset ${config.id}")
			assertEquals(config.count, config.timeTable.count)
		}
	}

	@Test
	fun builtInLookup() {
		assertEquals(TimeTablePresets.WINTER_NEW, TimeTablePresets.builtIn(TimeTablePresets.WINTER_NEW_ID))
		assertNotNull(TimeTablePresets.builtIn(TimeTablePresets.WINTER_OLD_ID))
		assertNotNull(TimeTablePresets.builtIn(TimeTablePresets.SUMMER_OLD_ID))
		assertNotNull(TimeTablePresets.builtIn(TimeTablePresets.GAOMI_WINTER_ID))
		assertNotNull(TimeTablePresets.builtIn(TimeTablePresets.GAOMI_SUMMER_ID))
		assertNull(TimeTablePresets.builtIn("nope"))
		assertNull(TimeTablePresets.builtIn(null))
	}
}
