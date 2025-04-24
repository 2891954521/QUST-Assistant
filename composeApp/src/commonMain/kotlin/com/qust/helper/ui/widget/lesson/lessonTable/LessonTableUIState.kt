package com.qust.helper.ui.widget.lesson.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.model.lessonTable.LessonTableModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Arrays

class LessonTableUIState {

	val timeTable by LessonTableModel._timeTable

	val startDay by mutableStateOf(LessonTableModel._startDay.value.toLocalDateTime(TimeZone.UTC).date)

	val currentWeek by LessonTableModel._currentWeek

	val totalWeek by LessonTableModel._totalWeek

	var lessonGroupRender by mutableStateOf(emptyList<LessonGroupRenderAble>())

	var lessons: List<Lesson> = mutableListOf()

	fun setLessonTable(lessons: List<Lesson>){
		this.lessons = lessons
		lessonGroupRender = lessons.mapIndexed { i, lesson ->
			val offset = timeTable.calcOffset(lesson)
			LessonGroupRenderAble(
				totalWeek,
				lesson.week,
				offset.first,
				offset.second,
				arrayOf(lesson),
				arrayOf(i)
			)
		}
	}

	fun appendLesson(newLesson: Lesson){
		val count = lessons.size
		lessons = List(count + 1){ if(it < count) lessons[it] else newLesson }
		lessonGroupRender = List(count + 1) { if(it < count) lessonGroupRender[it] else {
				val offset = timeTable.calcOffset(newLesson)
				LessonGroupRenderAble(
					totalWeek,
					newLesson.week,
					offset.first,
					offset.second,
					arrayOf(newLesson),
					arrayOf(count)
				)
			}
		}
	}

	fun updateLesson(index: Int, newLesson: Lesson){
		lessons = lessons.mapIndexed { i, lesson -> if(i == index) newLesson else lesson }
		lessonGroupRender = lessonGroupRender.mapIndexed { i, oldLesson -> if(i == index) {
				val offset = timeTable.calcOffset(newLesson)
				LessonGroupRenderAble(
					totalWeek,
					newLesson.week,
					offset.first,
					offset.second,
					arrayOf(newLesson),
					arrayOf(index)
				)
			}else oldLesson
		}
	}
}

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 * @param week 星期几 1-7
 */
class LessonGroupRenderAble(
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
	 * 当前正在展示的是第几节课，从0开始
	 */
	val index = IntArray(totalWeek).also { Arrays.fill(it, -1) }

	/**
	 * 某一周的这个时间点同时有课的课
	 * 5 = 101，表示当第1，3课会上，第2课不会上
	 */
	private val lessonTime = IntArray(totalWeek)

	init {
		var offset = 1
		lessons.forEachIndexed { i, lesson ->
			var week = 1L
			for(j in 0 until totalWeek) {
				if(lesson.weeks and week > 0) {
					if(index[j] == -1) index[j] = i
					lessonTime[j] = lessonTime[j] or offset
					lessonCount[j]++
				}
				week = week shl 1
			}
			offset = offset shl 1
		}
	}

	fun current(weekOfTerm: Int): Int {
		return if(lessonCount[weekOfTerm] == 0) -1 else index[weekOfTerm]
	}

	override fun toString(): String {
		return lessons.contentToString()
	}
}
