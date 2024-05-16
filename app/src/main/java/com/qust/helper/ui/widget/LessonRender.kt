package com.qust.helper.ui.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qust.helper.data.Data
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.theme.BACKGROUND_COLORS
import com.qust.helper.ui.theme.BACKGROUND_COLOR_SECOND
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.theme.TEXT_COLOR_SECOND
import java.util.Date

/**
 * 总课表界面的渲染器
 */
class LessonRender(
	startDay: MutableState<Date> = LessonTableRepository._startDay,
	totalWeek: MutableIntState = LessonTableRepository._totalWeek,
	lessonTable: MutableState<LessonTable> = LessonTableRepository._lessonTable,
) {

	private var baseLine = 0
	private var textHeight = 0

	/**
	 * 左侧时间和顶部日期的宽度
	 */
	private var timeWidth = 0
	private var dateHeight = 0

	/**
	 * 最小的一节课的大小
	 */
	private var cellWidth = 0
	private var cellHeight = 0

	/**
	 * 不同文本之间的间距
	 */
	private var linePadding = 0

	private var composeWidth: Int = 0
	private var composeHeight: Int = 0

	private val paint: Paint = Paint(Paint.FILTER_BITMAP_FLAG).also { paint ->
		paint.style = Paint.Style.FILL
		paint.isAntiAlias = true
		paint.strokeWidth = 3f
	}

	private val paintT: Paint = Paint().also { paintT ->
		paintT.isDither = true
		paintT.isAntiAlias = true
		paintT.isSubpixelText = true
		paintT.textAlign = Paint.Align.CENTER
	}

	private val hideTeacher by LessonTableRepository._hideTeacher
	private val showAllLesson by LessonTableRepository._showAllLesson
	private val hideFinishLesson by LessonTableRepository._hideFinishLesson

	private var _startDay = startDay
	val startDay: Date
		get() = _startDay.value

	private var _totalWeek = totalWeek
	val totalWeek: Int
		get() = _totalWeek.intValue

	private var _lessonTable = lessonTable
	val lessonTable: LessonTable
		get() = _lessonTable.value

	private var lessonRenderData by mutableStateOf(LessonRenderData(hideTeacher, paintT))

	val lockLesson by LessonTableRepository._lockLesson

	/**
	 * 设置 View Measure 数据
	 * 必须调用，不然无法显示
	 */
	fun setMeasureData(measuredWidth: Int, measuredHeight: Int, density: Density) {
		if(measuredWidth == composeWidth && measuredHeight == composeHeight) return
		with(density) {
			linePadding = 4.dp.toPx().toInt()
			paintT.textSize = 12.sp.toPx()
			baseLine = (paintT.textSize / 2 + (paintT.fontMetrics.descent - paintT.fontMetrics.ascent) / 2 - paintT.fontMetrics.descent).toInt()
			textHeight = (paintT.textSize + 3).toInt()
		}
		composeWidth = measuredWidth
		composeHeight = measuredHeight
		cellWidth = (measuredWidth - timeWidth) / 7
		cellHeight = (measuredHeight - dateHeight) / 10
		lessonRenderData = LessonRenderData(hideTeacher, paintT, (cellWidth - (LESSON_PADDING shl 2)), lessonTable)
		lessonRenderData.calcLessonData()
	}

	fun updateLessonTable() {
		if(cellWidth != 0 && cellHeight != 0) {
			lessonRenderData = LessonRenderData(hideTeacher, paintT, (cellWidth - (LESSON_PADDING shl 2)), lessonTable)
			lessonRenderData.calcLessonData()
		}
	}

	/**
	 * 获取点击位置的课程
	 * @param week 当前周(从0开始)
	 * @param downX 点击X坐标
	 * @param downY 点击Y坐标
	 */
	fun getClickLesson(week: Int, downX: Int, downY: Int): SelectLesson {
		if(downX < timeWidth || downY < dateHeight) {
			return SelectLesson(-1, -1, null)
		}

		// 计算点击的位置是星期几
		val dayOfWeek = (downX - timeWidth) / cellWidth
		if(dayOfWeek >= Data.WEEK_STRING.size) {
			return SelectLesson(-1, -1, null)
		}
		var y = downY - dateHeight

		for(timeSlot in lessonRenderData.lessons[dayOfWeek].indices) {
			lessonRenderData.lessons[dayOfWeek][timeSlot]?.let{ holder ->
				var lessonData = holder.current(week)
				if(lessonData == null && showAllLesson) {
					lessonData = holder.findLesson(week, !hideFinishLesson)
				}
				if(lessonData != null) {
					if(y < lessonData.len * cellHeight) {
						return SelectLesson(dayOfWeek, timeSlot, lessonRenderData.getLessonByHolder(week, dayOfWeek, timeSlot))
					}
				}
			}

			if(y < cellHeight) {
				return SelectLesson(dayOfWeek, timeSlot, null)
			}
			y -= cellHeight
		}
		return SelectLesson(-1, -1, null)
	}

	fun hasNextLesson(week: Int, dayOfWeek: Int, timeSlot: Int): Boolean {
		return lessonRenderData.lessons[dayOfWeek][timeSlot]?.hasNext(week) ?: false
	}

	fun nextLesson(week: Int, dayOfWeek: Int, timeSlot: Int): Lesson {
		lessonRenderData.lessons[dayOfWeek][timeSlot]?.next(week)
		return lessonRenderData.getLessonByHolder(week, dayOfWeek, timeSlot)
	}

	/**
	 * 绘制View
	 * @param week 绘制第几周，从0开始
	 */
	fun drawView(canvas: Canvas, week: Int) {
		var x = timeWidth
		for(i in lessonRenderData.lessons.indices) {
			var y = dateHeight
			for(j in lessonRenderData.lessons[0].indices) {

				val holder: LessonRenderData.LessonHolder? = lessonRenderData.lessons[i][j]
				if(holder == null) { y += cellHeight; continue }

				var lesson = holder.current(week)
				if(lesson == null) {
					if(!showAllLesson) { y += cellHeight; continue }
					lesson = holder.findLesson(week, !hideFinishLesson)
					if(lesson == null) { y += cellHeight; continue }

					paint.color = BACKGROUND_COLOR_SECOND
					paintT.color = TEXT_COLOR_SECOND
				} else {
					paint.color = BACKGROUND_COLORS[lesson.color]
					paintT.color = TEXT_COLORS[lesson.color]
				}

				canvas.drawRoundRect((x + LESSON_PADDING).toFloat(), (y + LESSON_PADDING).toFloat(), (x + cellWidth - LESSON_PADDING).toFloat(), (y + cellHeight * lesson.len - LESSON_PADDING).toFloat(), 16f, 16f, paint)
				canvas.drawText(if(lesson.type == 0) "A" else "U", (x + (LESSON_PADDING shl 2) + 3).toFloat(), (y + baseLine + (LESSON_PADDING shl 2)).toFloat(), paintT)

				if(holder.lessonCount[week] > 1) {
					canvas.drawText((holder.index[week] + 1).toString() + "/" + holder.lessonCount[week], (x + cellWidth / 2).toFloat(), (y + cellHeight * lesson.len - textHeight + baseLine - (LESSON_PADDING shl 2)).toFloat(), paintT)
				}

				var lineY: Int = y + baseLine + (cellHeight * lesson.len - textHeight * lesson.lines - linePadding * if(hideTeacher) 1 else 2) / 2
				for(n in lesson.data.indices) {
					lineY += if(lesson.data[n] == null) {
						linePadding
					} else {
						canvas.drawText(lesson.data[n]!!, (x + cellWidth / 2).toFloat(), lineY.toFloat(), paintT)
						textHeight
					}
				}
				y += cellHeight
			}
			x += cellWidth
		}
	}

	/**
	 * 绘制选中高亮框
	 * @param week 星期几
	 * @param count 第几节
	 * @param len 课程长度
	 */
	fun drawHighlightBox(canvas: Canvas, week: Int, count: Int, len: Int) {
		paint.style = Paint.Style.STROKE
		paint.color = Color.rgb(0, 176, 255)
		canvas.drawRoundRect(
			(week * cellWidth + timeWidth + LESSON_PADDING).toFloat(),
			(count * cellHeight + dateHeight + LESSON_PADDING).toFloat(),
			(week * cellWidth + cellWidth + timeWidth - LESSON_PADDING).toFloat(),
			(count * cellHeight + cellHeight * len + dateHeight - LESSON_PADDING).toFloat(),
			16f,
			16f,
			paint
		)
		paint.style = Paint.Style.FILL
	}


	data class SelectLesson(
		val dayOfWeek: Int,
		val timeSlot: Int,
		val lesson: Lesson?
	)


	companion object {
		/**
		 * 课程间距
		 */
		private const val LESSON_PADDING = 3
	}
}
