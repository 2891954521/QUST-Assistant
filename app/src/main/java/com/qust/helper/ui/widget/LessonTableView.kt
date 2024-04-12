package com.qust.helper.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.ui.widget.Texts.SingleLineText
import java.util.Calendar
import java.util.Date

object LessonTableView{

	/**
	 * 课程时间表
	 */
	val LESSON_TIME1 = arrayOf(
		arrayOf("08:00", "09:00", "10:10", "11:10", "13:30", "14:30", "15:40", "16:40", "18:00", "19:00"),
		arrayOf("08:50", "09:50", "11:00", "12:00", "14:20", "15:20", "16:30", "17:30", "18:50", "19:50")
	)

	val LESSON_TIME2 = arrayOf(
		arrayOf("08:00", "09:00", "10:10", "11:10", "14:00", "15:00", "16:10", "17:10", "18:30", "19:30"),
		arrayOf("08:50", "09:50", "11:00", "12:00", "14:50", "15:50", "17:00", "18:00", "19:20", "20:20")
	)

	@Composable
	@OptIn(ExperimentalFoundationApi::class)
	fun LessonView(lessonRender: LessonRender, onLessonClick: (LessonRender.SelectLesson) -> Unit = {}, onLessonLongClick: (LessonRender.SelectLesson, Int, Int, Int) -> Unit = { _, _, _, _ -> }){
		val density = LocalDensity.current
		val pagerState = rememberPagerState(initialPage = LessonTableRepository.currentWeek.intValue - 1, pageCount = { lessonRender.totalWeek })
		var selectLesson by remember { mutableStateOf(LessonRender.SelectLesson(-1, -1, null)) }

		LaunchedEffect(lessonRender.lessonTable){
			lessonRender.updateLessonTable()
		}

		Column {
			SingleLineText(
				text = stringResource(id = R.string.text_week, pagerState.currentPage + 1),
				modifier = Modifier.fillMaxWidth(),
				style = MaterialTheme.typography.titleLarge
			)
			HorizontalPager(
				state = pagerState,
				modifier = Modifier.fillMaxSize()
			) { page ->
				Tab(timeBar = {
					LessonTimeBar(if(LessonTableRepository.currentTimeTable == 0) LESSON_TIME1 else LESSON_TIME2)
				}, dateBar = {
					LessonDate(lessonRender.startDay, page)
				}) {
					Canvas(modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
						lessonRender.setMeasureData(coordinates.size.width, coordinates.size.height, density)
					}.pointerInput(Unit) {
						detectTapGestures(
							onTap = { offset ->
								val x = offset.x.toInt()
								val y = offset.y.toInt()
								val select = lessonRender.getClickLesson(page, x, y)
								if(select.dayOfWeek != -1 && select.timeSlot != -1 && lessonRender.hasNextLesson(page, select.dayOfWeek, select.timeSlot)){
									selectLesson = LessonRender.SelectLesson(select.dayOfWeek, select.timeSlot, lessonRender.nextLesson(page, select.dayOfWeek, select.timeSlot))
									return@detectTapGestures
								}else if(selectLesson.dayOfWeek == select.dayOfWeek && selectLesson.timeSlot == select.timeSlot) {
									onLessonClick(selectLesson)
								}
								selectLesson = select
							},
							onLongPress = { offset ->
								selectLesson = lessonRender.getClickLesson(page, offset.x.toInt(), offset.y.toInt())
								onLessonLongClick(selectLesson, page, offset.x.toInt(), offset.y.toInt())
							}
						)
					}){
						drawIntoCanvas{ canvas ->
							lessonRender.drawView(canvas = canvas.nativeCanvas, page)
							if(selectLesson.dayOfWeek != -1 && selectLesson.timeSlot != -1) {
								lessonRender.drawHighlightBox(canvas.nativeCanvas, selectLesson.dayOfWeek, selectLesson.timeSlot, selectLesson.lesson?.len ?: 1)
							}
						}
					}
				}
			}
		}
	}

	@Composable
	fun Tab(timeBar: @Composable () -> Unit, dateBar: @Composable () -> Unit, lessonTable: @Composable () -> Unit,) {
		SubcomposeLayout(measurePolicy = { constraints ->
			val timeWidth: Int
			subcompose(SlotsEnum.Dependent, timeBar).first().let { measureAble ->
				measureAble.measure(constraints).also { timeWidth = it.width }
			}

			val dateHeight: Int
			val dateBars = subcompose(SlotsEnum.Date, dateBar)
			val dateBarPlace = dateBars[1].let{ measureAble ->
				measureAble.measure(constraints.copy(maxWidth = (constraints.maxWidth - timeWidth))).also { dateHeight = it.height }
			}
			val monthBarPlace = dateBars[0].measure(Constraints(timeWidth, timeWidth, dateHeight, dateHeight))

			val timeBarPlace = subcompose(SlotsEnum.Time, timeBar).first().measure(
				Constraints(timeWidth, timeWidth, (constraints.maxHeight - dateHeight), (constraints.maxHeight - dateHeight))
			)

			val lessonTablePlace = subcompose(SlotsEnum.Main, lessonTable).first().measure(
				constraints.copy(maxWidth = (constraints.maxWidth - timeWidth), maxHeight = (constraints.maxHeight - dateHeight))
			)

			layout(constraints.maxWidth, constraints.maxHeight) {
				monthBarPlace.place(0, 0)
				dateBarPlace.place(timeWidth, 0)
				timeBarPlace.place(0, dateHeight)
				lessonTablePlace.place(timeWidth, dateHeight)
			}
		})
	}

	@Composable
	fun LessonDate(startDay: Date, week: Int) {
		val currentDay = Calendar.getInstance()
		val c = Calendar.getInstance().also { it.time = startDay }
		c[Calendar.WEEK_OF_YEAR] += week
		c[Calendar.DATE] -= (c[Calendar.DAY_OF_WEEK] - 2)

		Box{
			Text(
				text = "${c[Calendar.MONTH] + 1}月",
				modifier = Modifier.align(Alignment.Center),
			)
		}

		Row(modifier = Modifier.fillMaxWidth()) {
			repeat(Data.WEEK_STRING.size) {
				val color = if(currentDay[Calendar.DATE] == c[Calendar.DATE] && currentDay[Calendar.MONTH] == c[Calendar.MONTH]) {
					LESSON_TEXT_COLORS[0]
				} else Color.Gray

				Column(modifier = Modifier.weight(1F).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
					Text(text = Data.WEEK_STRING[it], style = MaterialTheme.typography.bodySmall, color = color)
					Text(text = if(c[Calendar.DATE] == 1) "${c[Calendar.MONTH] + 1}月" else "${c[Calendar.DATE]}", style = MaterialTheme.typography.bodySmall, color = color)
				}
				c[Calendar.DATE] += 1
			}
		}
	}

	@Composable
	fun LessonTimeBar(time: Array<Array<String>>) {
		Column(modifier = Modifier.fillMaxHeight()) {
			repeat(LESSON_TIME1[0].size){
				Column(
					modifier = Modifier.weight(1F).padding(horizontal = 4.dp),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(text = (it + 1).toString(), style = MaterialTheme.typography.labelMedium)
					Text(text = LESSON_TIME1[0][it], style = MaterialTheme.typography.bodySmall, color = Color.Gray)
					Text(text = LESSON_TIME1[1][it], style = MaterialTheme.typography.bodySmall, color = Color.Gray)
				}
			}
		}
	}

	enum class SlotsEnum { Dependent, Month, Time, Date, Main }

}