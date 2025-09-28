package com.qust.helper.ui.widget.lesson.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.repository.LessonTableRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import java.util.Arrays

data class LessonTableInfo(
	/** 时间表 */
	val timeTable: TimeTable = TimeTable.Companion.DEFAULT,
	/** 开学时间 */
	val startDay: LocalDate = SettingModel.startDay,
	/** 总周数 */
	val totalWeek: Int = SettingModel.totalWeek,
	/** 当前周 (从 0 开始) */
	val currentWeek: Int = 0,

	val lessons: List<Lesson> = emptyList()
)

class LessonTableUIState(
	val lessonTableInfo: StateFlow<LessonTableInfo> = LessonTableRepository.currentLessonTable
) {
	var lessonGroupRender by mutableStateOf(emptyList<LessonGroupRenderAble>())

	fun refreshLessonTable(){
		lessonGroupRender = lessonTableInfo.value.lessons.mapIndexed { i, lesson ->
			val offset = lessonTableInfo.value.timeTable.calcOffset(lesson)
			LessonGroupRenderAble(
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
