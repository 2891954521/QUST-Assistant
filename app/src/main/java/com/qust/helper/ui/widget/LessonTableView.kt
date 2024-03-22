package com.qust.helper.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.qust.helper.R
import com.qust.helper.ui.common.Texts.SingleLineText

object LessonTableView{

	@Composable
	fun LessonView(lessonRender: LessonRender){
		val density = LocalDensity.current
		val pagerState = rememberPagerState(pageCount = { lessonRender.totalWeek })

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
				Canvas(
					modifier = Modifier.fillMaxSize()
				){
					drawIntoCanvas{ canvas ->
						lessonRender.drawView(canvas = canvas.nativeCanvas, page)
					}
				}
			}
		}
	}
}