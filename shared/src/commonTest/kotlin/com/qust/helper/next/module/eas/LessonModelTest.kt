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

	/**
	 * 只有一周上课：第x周（教务返回 "第1周" 这类带"第"的格式时不能解析失败）
	 */
	@Test
	fun singleWeekWithPrefix() {
		assertEquals(weeksOf(3), LessonModel.parseWeeks("第3周"))
	}

	@Test
	fun singleWeekWithoutPrefix() {
		assertEquals(weeksOf(3), LessonModel.parseWeeks("3周"))
	}

	/**
	 * 连续多周：x-x周
	 */
	@Test
	fun continuousWeeks() {
		assertEquals(weeksOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), LessonModel.parseWeeks("1-16周"))
	}

	/**
	 * 间断多周：x-x周,x周,x-x周
	 */
	@Test
	fun discontinuousWeeks() {
		assertEquals(weeksOf(1, 2, 3, 4, 5, 10), LessonModel.parseWeeks("1-4周,5周,10-10周"))
	}

	/**
	 * 重复段取并集，不能互相抵消
	 */
	@Test
	fun overlappingSegmentsAreMerged() {
		assertEquals(weeksOf(1, 2, 3, 4), LessonModel.parseWeeks("1-4周,1-2周"))
	}

	/**
	 * 单双周：x-x周(单),x-x周(双)
	 */
	@Test
	fun oddWeeks() {
		assertEquals(weeksOf(1, 3, 5, 7, 9, 11, 13, 15), LessonModel.parseWeeks("1-16周(单)"))
	}

	@Test
	fun evenWeeks() {
		assertEquals(weeksOf(2, 4, 6, 8, 10, 12, 14, 16), LessonModel.parseWeeks("1-16周(双)"))
	}

	/**
	 * 奇偶起点与标记不一致时也要对齐
	 */
	@Test
	fun oddWeeksStartingFromEvenWeek() {
		assertEquals(weeksOf(3, 5), LessonModel.parseWeeks("2-5周(单)"))
	}

	@Test
	fun evenWeeksStartingFromOddWeek() {
		assertEquals(weeksOf(2, 4), LessonModel.parseWeeks("1-5周(双)"))
	}

	/**
	 * 多段混合：第x周 + 连续 + 单双周
	 */
	@Test
	fun mixedFormats() {
		assertEquals(weeksOf(1, 3, 5, 7), LessonModel.parseWeeks("第1周,3周,5-8周(单)"))
	}

	/**
	 * 解析结果要真的写进 Lesson.weeks
	 */
	@Test
	fun lessonWeeksComeFromZcd() {
		val lessons = LessonModel.loadFromJson(jsonWithJcs("1-2", zcd = "第2周"))
		assertNotNull(lessons)

		assertEquals(weeksOf(2), lessons.single().weeks)
	}

	private fun weeksOf(vararg weeks: Int): Long {
		var mask = 0L
		for(week in weeks) mask = mask or (1L shl (week - 1))
		return mask
	}

	private fun jsonWithJcs(jcs: String, zcd: String = "1周") = buildJsonObject {
		put("kbList", buildJsonArray {
			addJsonObject {
				put("jcs", jcs)
				put("kcmc", "测试课程")
				put("kch", "K001")
				put("xqj", 1)
				put("zcd", zcd)
			}
		})
	}
}
