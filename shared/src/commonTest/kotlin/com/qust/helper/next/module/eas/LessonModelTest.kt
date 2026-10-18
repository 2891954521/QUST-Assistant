package com.qust.helper.next.module.eas

import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.entity.lesson.TimeTablePresets
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * 教务课表解析：节次 → 时间的边界处理
 */
class LessonModelTest {

	private val tenPeriods = TimeTablePresets.WINTER_OLD.timeTable

	@Test
	fun beyondTablePeriodIsClamped() {
		val lessons = LessonModel.loadFromJson(jsonWithJcs("11-11"), tenPeriods)
		assertNotNull(lessons)

		val lesson = lessons.single()
		assertEquals(tenPeriods.startMinute.last(), lesson.startMinute)
		assertEquals(tenPeriods.endMinute.last(), lesson.endMinute)
	}

	@Test
	fun singleSectionWithoutDash() {
		val lessons = LessonModel.loadFromJson(jsonWithJcs("3"), tenPeriods)
		assertNotNull(lessons)

		val lesson = lessons.single()
		assertEquals(tenPeriods.startMinute[2], lesson.startMinute)
		assertEquals(tenPeriods.endMinute[2], lesson.endMinute)
	}

	@Test
	fun spannedSectionsUseBothEnds() {
		val lessons = LessonModel.loadFromJson(jsonWithJcs("1-2"), tenPeriods)
		assertNotNull(lessons)

		val lesson = lessons.single()
		assertEquals(tenPeriods.startMinute[0], lesson.startMinute)
		assertEquals(tenPeriods.endMinute[1], lesson.endMinute)
	}

	@Test
	fun emptyTimeTableReturnsNull() {
		assertNull(LessonModel.loadFromJson(jsonWithJcs("1-1"), TimeTable(emptyArray(), emptyArray())))
	}

	private fun jsonWithJcs(jcs: String) = buildJsonObject {
		put("kbList", buildJsonArray {
			addJsonObject {
				put("jcs", jcs)
				put("kcmc", "测试课程")
				put("kch", "K001")
				put("xqj", 1)
				put("zcd", "1周")
			}
		})
	}
}
