package com.qust.helper.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.qust.helper.R
import com.qust.helper.ui.common.Texts.SingleLineText

object LessonTableView{

	@Composable
	fun LessonView(lessonRender: LessonRender, onLessonClick: (LessonRender.SelectLesson) -> Unit = {}, onLessonLongClick: (LessonRender.SelectLesson, Int, Int, Int) -> Unit = { _, _, _, _ -> }){
		val density = LocalDensity.current
		val pagerState = rememberPagerState(pageCount = { lessonRender.lessonTable?.totalWeek ?: 1 })
		var selectLesson by remember { mutableStateOf(LessonRender.SelectLesson(-1, -1, null)) }

		Column {
			SingleLineText(
				text = stringResource(id = R.string.text_week, pagerState.currentPage + 1),
				modifier = Modifier.fillMaxWidth(),
				style = MaterialTheme.typography.titleLarge
			)

			HorizontalPager(
				state = pagerState,
				modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
					lessonRender.setMeasureData(coordinates.size.width, coordinates.size.height, density)
				}
			) { page ->
				Canvas(modifier = Modifier.fillMaxSize()
					.pointerInput(Unit) {
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
					}
				){
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