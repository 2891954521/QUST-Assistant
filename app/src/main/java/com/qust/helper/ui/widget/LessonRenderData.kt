package com.qust.helper.ui.widget

import android.graphics.Paint
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.data.lesson.LessonTable
import java.util.Arrays

/**
 * 课程渲染信息
 */
class LessonRenderData(
	/**
	 * 隐藏教师
	 */
	private val hideTeacher: Boolean,
	private val textPaint: Paint,
	private val cellWidth: Int = 0,
	val lessonTable: LessonTable? = null
) {

	var lessons: Array<Array<LessonHolder?>> = Array(7) { arrayOfNulls(10) }

	/**
	 * 计算绘制课程的信息
	 */
	fun calcLessonData() {
		lessonTable?.let {
			val lessonGroups = it.lessons
			lessons = Array(lessonGroups.size) { arrayOfNulls(lessonGroups[0].size) }
			for(dayOfWeek in lessonGroups.indices) {
				for(timeSlot in lessonGroups[0].indices) {
					lessonGroups[dayOfWeek][timeSlot]?.let{ lessonGroup ->
						lessons[dayOfWeek][timeSlot] = LessonHolder(lessonGroup, it.totalWeek)
					}
				}
			}
		}
	}

	/**
	 * 从用于渲染的LessonHolder中获取课表中实际的Lesson
	 * @param dayOfWeek
	 * @param timeSlot
	 * @return
	 */
	fun getLessonByHolder(week: Int, dayOfWeek: Int, timeSlot: Int): Lesson {
		return lessonTable!!.lessons[dayOfWeek][timeSlot]!!.lessons[lessons[dayOfWeek][timeSlot]!!.index[week]]
	}

	/**
	 * 字符串分行
	 * @param src 要分行的字符串
	 * @param des 分行后的字符串
	 * @param maxWidth 一行最大的长度
	 * @param desPos 目标数组开始的位置
	 * @param maxLine 最大支持的行数
	 */
	private fun splitString(src: String, des: Array<String?>, maxWidth: Int, desPos: Int, maxLine: Int): Int {
		var srcPos = 0
		var lines = 0
		val length = src.length
		var i = 0
		while(i < length) {
			i = srcPos + textPaint.breakText(src, i, length, true, maxWidth.toFloat(), null)
			if(desPos + lines >= des.size) return lines
			des[desPos + lines] = src.substring(srcPos, i)
			srcPos = i
			if(lines++ == maxLine) return lines
		}
		return lines
	}

	/**
	 * 课程组
	 */
	inner class LessonHolder(
		lessonGroup: LessonGroup,
		totalWeek: Int
	) {

		/**
		 * 某一周的这个时间点同时有几节课
		 */
		var lessonCount = IntArray(totalWeek)

		/**
		 * 当前正在展示的是第几节课，从0开始
		 */
		var index = IntArray(totalWeek).also { Arrays.fill(it, -1) }

		/**
		 * 某一周的这个时间点同时有课的课
		 * 5 = 101，表示当第1，3课会上，第2课不会上
		 */
		private var lessonTime = IntArray(totalWeek)

		private var lessonData: Array<LessonData>

		init {
			var offset = 1
			val array = ArrayList<LessonData>(lessonGroup.lessons.size)
			lessonGroup.lessons.forEachIndexed { i, lesson ->
				array.add(LessonData(lesson))
				var week = 1L
				for(j in 0 until totalWeek) {
					if(lesson.week and week > 0) {
						if(index[j] == -1) index[j] = i
						lessonTime[j] = lessonTime[j] or offset
						lessonCount[j]++
					}
					week = week shl 1
				}
				offset = offset shl 1
			}
			lessonData = array.toTypedArray()
		}

		/**
		 * 是否有多节课
		 */
		fun hasNext(week: Int): Boolean {
			return lessonCount[week] > 1
		}

		/**
		 * 显示下一节课
		 */
		fun next(week: Int) {
			val currentIndex = (index[week] + 1)
			var offset = 1 shl currentIndex
			for(i in currentIndex until  lessonCount[week]) {
				if(lessonTime[week] and offset > 0) {
					index[week] = i
					return
				}
				offset = offset shl 1
			}
			index[week] = 0
		}

		fun current(week: Int): LessonData? {
			return if(lessonCount[week] == 0) null else lessonData[index[week]]
		}

		/**
		 * 查找最接近当前周会上的课程
		 *
		 * @param week 当前周
		 * @param findAll 查找全部时间
		 */
		fun findLesson(week: Int, findAll: Boolean): LessonData? {
			if(index[week] != -1) return lessonData[index[week]]

			// 向后查找课程
			for(i in (week + 1) until lessonCount.size) {
				if(lessonTime[i] > 0) {
					val pos = java.lang.Long.numberOfTrailingZeros(lessonTime[i].toLong())
					index[week] = pos
					return lessonData[pos]
				}
			}

			// 向前查找课程
			if(findAll && week > 0) {
				for(i in (week - 1) downTo 0) {
					if(lessonTime[i] > 0) {
						val pos = java.lang.Long.numberOfTrailingZeros(lessonTime[i].toLong())
						index[week] = pos
						return lessonData[pos]
					}
				}
			}
			return null
		}
	}

	/**
	 * 课程信息
	 * @param type 课程类型
	 * @param len 课程长度
	 * @param color 课程颜色
	 */
	inner class LessonData(
		lesson: Lesson,
		val type: Int = lesson.type,
		val len: Int = lesson.len,
		val color: Int = lesson.color,
	) {
		/**
		 * 文本信息的行数
		 */
		var lines: Int

		/**
		 * 课程文本信息
		 */
		var data: Array<String?> = arrayOfNulls(len * 3 + if(hideTeacher) 1 else 2)

		init {
			val l = data.size
			lines = splitString(lesson.name, data, cellWidth, 0, l - 3)
			lines += splitString(lesson.place, data, cellWidth, lines + 1, l - lines - 1)
			if(!hideTeacher) {
				lines += splitString(lesson.teacher, data, cellWidth, lines + 2, l - lines)
			}
		}
	}
}