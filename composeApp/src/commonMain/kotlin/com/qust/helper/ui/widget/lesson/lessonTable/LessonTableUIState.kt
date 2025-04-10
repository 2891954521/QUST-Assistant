package com.qust.helper.ui.widget.lesson.lessonTable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.model.lessonTable.LessonTableModel

class LessonTableUIState {

	val timeTable by LessonTableModel._timeTable

	val startDay by LessonTableModel._startDay

	val currentWeek by LessonTableModel._currentWeek

	val totalWeek by LessonTableModel._totalWeek

	var lessonGroupRender by mutableStateOf(emptyList<LessonGroupRenderAble>())

	fun setLessonTable(lessons: List<Lesson>){
		val diff = List(timeTable.count){ (timeTable.endMinute[it] - timeTable.startMinute[it]).toFloat() }
		lessonGroupRender = lessons.map { lesson ->

			// 找开始的时间是时间表里的第几个，以小数表示不足一个的时间
			val st = lesson.startMinute
			var startOffset = 0F
			var s = 0
			while(s < diff.size){
				if(st <= timeTable.startMinute[s]){
					// 小于开始节点，即为第 s 个
					startOffset = s.toFloat()
					break
				}else if(st < timeTable.endMinute[s]){
					// 小于结束节点，即为第 s + 多出的部分 个
					startOffset = s + (st - timeTable.startMinute[s]) / diff[s]
					break
				}else{
					// 大于结束节点的情况下继续查找下一个节点
				}
				s++
			}

			// 结束时间同理，从开始时间的位置往后找，防止无效查找
			val ed = lesson.endMinute
			var endOffset = timeTable.count.toFloat()
			for(i in s ..< diff.size){
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
