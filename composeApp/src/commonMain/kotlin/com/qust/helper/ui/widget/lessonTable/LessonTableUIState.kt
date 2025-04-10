package com.qust.helper.ui.widget.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import java.util.Date

class LessonTableUIState {

	val timeTable by mutableStateOf(TimeTable.DEFAULT)

	val startDay by mutableStateOf(Date())

	val currentWeek by mutableStateOf(0)

	val totalWeek by mutableStateOf(20)

	var lessonGroupRender by mutableStateOf(emptyList<LessonGroupRenderAble>())

	fun setLessonTable(lessons: List<Lesson>){
		val diff = List(timeTable.count){ (timeTable.endMinute[it] - timeTable.startMinute[it]).toFloat() }
		lessonGroupRender = lessons.map { lesson ->

			val st = lesson.startMinute
			var startOffset = 0F
			for(i in diff.indices){
				if(st <= timeTable.startMinute[i]){
					startOffset = i.toFloat()
					break
				}else if(st < timeTable.endMinute[i]){
					startOffset = (st - timeTable.startMinute[i]) / diff[i] + i
					break
				}
			}

			val ed = lesson.endMinute
			var endOffset = 0F
			for(i in diff.indices){
				if(ed <= timeTable.startMinute[i]){
					endOffset = i.toFloat()
					break
				}else if(ed < timeTable.endMinute[i]){
					endOffset = (ed - timeTable.startMinute[i]) / diff[i] + i
					break
				}
			}

			LessonGroupRenderAble(lesson.week, startOffset, endOffset, arrayOf(LessonRenderAble(
				colorIndex = lesson.colorLabel,
				name = lesson.name,
				place = lesson.place,
				teacher = lesson.teacher
			)))
		}

		println(lessonGroupRender)
	}
}


data class LessonRenderAble(
	val colorIndex: Int,
	val name: String,
	val place: String,
	val teacher: String,
)

/**
 * 一个时间的课程组
 * 表示一个时间点的N节不同的课程
 * @param week 星期几 1-7
 */
data class LessonGroupRenderAble(
	val week: Int,
	val startOffset: Float,
	val endOffset: Float,
	val lessons: Array<LessonRenderAble> = emptyArray()
)
