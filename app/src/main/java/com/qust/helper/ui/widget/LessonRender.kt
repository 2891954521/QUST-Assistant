package com.qust.helper.ui.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qust.helper.data.Data
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonTable
import com.qust.helper.ui.theme.BACKGROUND_COLORS
import com.qust.helper.ui.theme.BACKGROUND_COLOR_SECOND
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.theme.TEXT_COLOR_SECOND
import com.qust.helper.utils.DateUtils
import java.util.Calendar

/**
 * 总课表界面的渲染器
 */
class LessonRender {
	/**
	 * 今天的日期
	 */
	private val currentDay: Calendar = Calendar.getInstance()

	/**
	 * 显示全部课程
	 */
	private val showAllLesson: Boolean = Setting.getBoolean(Keys.KEY_SHOW_ALL_LESSON)

	/**
	 * 隐藏已结课程
	 */
	private val hideFinishLesson: Boolean = Setting.getBoolean(Keys.KEY_HIDE_FINISH_LESSON)

	/**
	 * 隐藏教师
	 */
	private val hideTeacher: Boolean = Setting.getBoolean(Keys.KEY_HIDE_TEACHER)

	private var baseLine = 0
	private var textHeight = 0

	/**
	 * 左侧时间和顶部日期的宽度
	 */
	var timeWidth = 0
	var dateHeight = 0

	/**
	 * 最小的一节课的大小
	 */
	private var cellWidth = 0
	private var cellHeight = 0

	/**
	 * 不同文本之间的间距
	 */
	private var linePadding = 0

	private val paint: Paint = Paint(Paint.FILTER_BITMAP_FLAG)
	private val paintT: Paint = Paint()

	private val timeText: Array<Array<String>> = Data.LESSON_TIME_TEXT[0]

	private val lessonRenderData: LessonRenderData

	var totalWeek: Int = 0

	init {
		paint.style = Paint.Style.FILL
		paint.isAntiAlias = true
		paint.strokeWidth = 3f

		paintT.isDither = true
		paintT.isAntiAlias = true
		paintT.isSubpixelText = true
		paintT.textAlign = Paint.Align.CENTER

		lessonRenderData = LessonRenderData(hideTeacher, paintT)
	}

	/**
	 * 设置 / 更新 课表信息
	 * @param lessonTable 课表信息
	 */
	fun setLessonTable(lessonTable: LessonTable) {
		lessonRenderData.lessonTable = lessonTable
		totalWeek = lessonTable.totalWeek
		if(cellWidth != 0 && cellHeight != 0) {
			lessonRenderData.cellWidth = (cellWidth - (LESSON_PADDING shl 2))
			lessonRenderData.calcLessonData()
		}
	}

	/**
	 * 设置 View Measure 数据
	 * 必须调用，不然无法显示
	 */
	fun setMeasureData(measuredWidth: Int, measuredHeight: Int, density: Density) {
		with(density) {
			timeWidth = 48.dp.toPx().toInt()
			linePadding = 4.dp.toPx().toInt()
			paintT.textSize = 12.sp.toPx()
			baseLine = (paintT.textSize / 2 + (paintT.fontMetrics.descent - paintT.fontMetrics.ascent) / 2 - paintT.fontMetrics.descent).toInt()
			textHeight = (paintT.textSize + 3).toInt()
			dateHeight = 40 + textHeight * 2
		}
		cellWidth = (measuredWidth - timeWidth) / Data.WEEK_STRING.size
		cellHeight = (measuredHeight - dateHeight) / timeText[0].size
		lessonRenderData.cellWidth = (cellWidth - (LESSON_PADDING shl 2))
		lessonRenderData.calcLessonData()
	}

	/**
	 * 获取点击位置的课程
	 * @param week 当前周
	 * @param downX 点击X坐标
	 * @param downY 点击Y坐标
	 * @param result 存放计算出的点击位置
	 * @return result 里为 [dayOfWeek, timeSlot]， return 值为点击到的课程
	 */
	fun getClickLesson(week: Int, downX: Int, downY: Int, result: IntArray): Lesson? {
		if(downX < timeWidth || downY < dateHeight) {
			result[0] = -1
			result[1] = -1
			return null
		}

		// 计算点击的位置是星期几
		val dayOfWeek = (downX - timeWidth) / cellWidth
		if(dayOfWeek >= Data.WEEK_STRING.size) {
			result[0] = -1
			result[1] = -1
			return null
		}
		result[0] = dayOfWeek
		var y = downY - dateHeight

		for(timeSlot in lessonRenderData.lessons[dayOfWeek].indices) {
			lessonRenderData.lessons[dayOfWeek][timeSlot]?.let{ holder ->
				var lessonData = holder.current(week)
				if(lessonData == null && showAllLesson) {
					lessonData = holder.findLesson(week, !hideFinishLesson)
				}
				if(lessonData != null) {
					if(y < lessonData.len * cellHeight) {
						result[1] = timeSlot
						return lessonRenderData.getLessonByHolder(week, dayOfWeek, timeSlot)
					}
				}
			}

			if(y < cellHeight) {
				result[1] = timeSlot
				return null
			}
			y -= cellHeight
		}
		result[0] = -1
		result[1] = -1
		return null
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
		drawTime(canvas)
		drawDate(canvas, week)
		drawLessons(canvas, week)
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

	protected fun drawTime(canvas: Canvas) {
		paintT.color = Color.GRAY
		val x = timeWidth / 2
		var y = dateHeight + baseLine + (cellHeight - textHeight * 2) / 2
		for(i in lessonRenderData.lessons[0].indices) {
			canvas.drawText(timeText[0][i], x.toFloat(), y.toFloat(), paintT)
			canvas.drawText(timeText[1][i], x.toFloat(), (y + textHeight).toFloat(), paintT)
			y += cellHeight
		}
	}

	protected fun drawDate(canvas: Canvas, week: Int) {
		val c = lessonRenderData.startDate.clone() as Calendar
		c.add(Calendar.WEEK_OF_YEAR, week)
		val dayOfWeek = c[Calendar.DAY_OF_WEEK] - 2
		c.add(Calendar.DATE, -dayOfWeek)
		val y = 20 + baseLine
		for(i in Data.WEEK_STRING.indices) {
			if(currentDay[Calendar.DATE] == c[Calendar.DATE] && currentDay[Calendar.MONTH] == c[Calendar.MONTH]) {
				paintT.color = TEXT_COLORS[0]
			} else {
				paintT.color = Color.GRAY
			}
			val day: String = DateUtils.MD.format(c.time)
			canvas.drawText(Data.WEEK_STRING[i], (timeWidth + cellWidth / 2 + i * cellWidth).toFloat(), y.toFloat(), paintT)
			canvas.drawText(day, (timeWidth + cellWidth / 2 + i * cellWidth).toFloat(), (y + textHeight).toFloat(), paintT)
			c.add(Calendar.DATE, 1)
		}
	}

	protected fun drawLessons(canvas: Canvas, week: Int) {
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

				if(holder.count[week] > 1) {
					canvas.drawText((holder.index[week] + 1).toString() + "/" + holder.count[week], (x + cellWidth / 2).toFloat(), (y + cellHeight * lesson.len - textHeight + baseLine - (LESSON_PADDING shl 2)).toFloat(), paintT)
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

	companion object {
		/**
		 * 课程间距
		 */
		private const val LESSON_PADDING = 3
	}
}
