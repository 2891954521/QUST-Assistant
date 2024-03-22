package com.qust.helper.data.lesson

import kotlinx.serialization.Serializable

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 * @param week 星期几 1-7
 * @param count 节次 1-10
 */
@Serializable
data class LessonGroup(
	var week: Int,
	var count: Int,
	var lessons: Array<Lesson> = emptyArray()
){

	/**
	 * 添加一节课程
	 */
	fun addLesson(lesson: Lesson){
		lessons += lesson
	}

	/**
	 * 移除一节课程
	 */
	fun removeLesson(lesson: Lesson){
		lessons = lessons.filterNot { it == lesson }.toTypedArray()
	}

	/**
	 * 移除指定位置的课程
	 */
	fun removeLesson(index: Int) {
		require(index >= 0 && index < lessons.size) { "Index out of bounds" }
		lessons = lessons.copyOfRange(0, index) + lessons.copyOfRange(index + 1, lessons.size)
	}

	/**
	 * 获取当前周会上的课程
	 * @param currentWeek 当前周（从 1 开始）
	 * @return 会上的课程, 没有课则为null
	 */
	fun getCurrentLesson(currentWeek: Int): Lesson? {
		for(lesson in lessons) {
			if(lesson.week and (1L shl (currentWeek - 1)) > 0) return lesson
		}
		return null
	}

	/**
	 * 将用户定义的课程合并到当前课程
	 * @param oldGroup
	 */
	fun mergeUserDefinedLesson(oldGroup: LessonGroup) {
		val mergedMap: MutableMap<String, Lesson> = HashMap()
		for(obj in lessons) mergedMap[obj.kchID] = obj
		for(obj in oldGroup.lessons) {
			if(obj.type == 1) mergedMap[obj.kchID] = obj
		}
		lessons = mergedMap.values.toTypedArray<Lesson>()
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as LessonGroup

		if(week != other.week) return false
		if(count != other.count) return false
		return lessons.contentEquals(other.lessons)
	}

	override fun hashCode(): Int {
		var result = week
		result = 31 * result + count
		result = 31 * result + lessons.contentHashCode()
		return result
	}

	companion object{
		val Empty = LessonGroup(0, 0)
	}
}
