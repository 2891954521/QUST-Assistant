package com.qust.helper.next.ui.business.lesson

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.LessonTableInfo
import com.qust.helper.next.repository.LessonTableRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.Arrays

class LessonTableUIState(
	val lessonTableInfo: StateFlow<LessonTableInfo> = LessonTableRepository.currentLessonTable
) {
	var lessonGroupRender by mutableStateOf(emptyList<LessonGroupRenderUIState>())

	fun refreshLessonTable(){
		lessonGroupRender = lessonTableInfo.value.lessons.mapIndexed { i, lesson ->
			val offset = lessonTableInfo.value.timeTable.calcOffset(lesson)
			LessonGroupRenderUIState(
				lessonTableInfo.value.totalWeek,
				lesson.week,
				offset.first,
				offset.second,
				arrayOf(lesson),
				arrayOf(i)
			)
		}
	}
}

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 * @param week 星期几 1-7
 */
@Immutable
class LessonGroupRenderUIState(
	totalWeek: Int,
	val week: Int,
	val startOffset: Float,
	val endOffset: Float,
	val lessons: Array<Lesson> = emptyArray(),
	val lessonIndex: Array<Int> = emptyArray()
){
	/**
	 * 某一周的这个时间点同时有几节课
	 */
	val lessonCount = IntArray(totalWeek)

	/**
	 * 当前正在展示的是第几节课，存放的 lessons 的索引
	 */
	val index = IntArray(totalWeek).also { Arrays.fill(it, -1) }

	/**
	 * 某一周的这个时间点同时有课的课
	 * 以二进制存储的布尔值列表
	 * 例如：13 = 0b1101，表示这个时间点 index 存放的第1、3、4课会上，第2课不会上
	 */
	private val lessonTime = IntArray(totalWeek)

	init {
		var offset = 1
		lessons.forEachIndexed { i, lesson ->
			var week = 1L
			for(j in 0 until totalWeek) {
				if((lesson.weeks and week) > 0) {
					if(index[j] == -1) index[j] = i
					lessonTime[j] = lessonTime[j] or offset
					lessonCount[j]++
				}
				week = week shl 1
			}
			offset = offset shl 1
		}
	}

	/**
	 * 获取当前周的课程的索引和是否是本周的课程
	 * 
	 * @param weekOfTerm 当前周
	 * @param showAllLesson 是否显示所有课程
	 * @param showFinish 是否显示已完成课程
	 */
	fun current(weekOfTerm: Int, showAllLesson: Boolean, showFinish: Boolean): Pair<Int, Boolean> {
		val lessonIndex = index[weekOfTerm]
		if(lessonIndex != -1) return Pair(lessonIndex, true)

		// 向后查找课程
		if(showAllLesson){
			for(i in (weekOfTerm + 1) until lessonTime.size) {
				if(lessonTime[i] > 0) {
					return Pair(Integer.numberOfTrailingZeros(lessonTime[i]), false)
				}
			}
		}

		// 向前查找课程
		if(showFinish && weekOfTerm > 0) {
			for(i in (weekOfTerm - 1) downTo 0) {
				if(lessonTime[i] > 0) {
					return Pair(Integer.numberOfTrailingZeros(lessonTime[i]), false)
				}
			}
		}

		return Pair(-1, false)
	}

	override fun toString(): String {
		return lessons.contentToString()
	}
}