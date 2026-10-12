package com.qust.helper.next.ui.business.lesson.table.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntRect
import com.qust.helper.next.common.Platform
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.lesson.LessonTableDefaults
import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.business.lesson.table.model.LessonTableLayout
import com.qust.helper.next.ui.business.lesson.table.state.LessonTableState
import com.qust.helper.next.ui.component.MultiPlatformPager
import kotlinx.datetime.LocalDate

@Composable
fun LessonTableFrame(
	layout: LessonTableLayout,
	settings: LessonTableRepository.LessonTableSetting,
	state: LessonTableState,
	colors: LessonTableColors,
	today: LocalDate,
	title: @Composable (week: Int) -> Unit,
	onCellClick: (LessonCellInfo) -> Unit,
	onCellLongClick: (LessonCellInfo, IntRect) -> Unit,
	modifier: Modifier = Modifier,
) {
	var tableCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
	Column(modifier.onGloballyPositioned { tableCoordinates = it }) {

		title(state.currentWeek)

		Row(Modifier.weight(1f).fillMaxWidth()) {

			BoxWithConstraints {
				val maxTimeBarWidth = maxWidth / 5
				val minTimeBarWidth = minOf(LessonTableDefaults.TimeBarWidth, maxTimeBarWidth)
				Column(
					Modifier
						.widthIn(min = minTimeBarWidth, max = maxTimeBarWidth)
						.width(IntrinsicSize.Max)
						.fillMaxHeight(),
				) {
					MonthCorner(
						modifier = Modifier.fillMaxWidth().height(LessonTableDefaults.DateBarHeight),
						startDay = layout.startDay,
						week = state.currentWeek,
					)
					LessonTimeBar(
						axis = layout.axis,
						colors = colors,
						modifier = Modifier.weight(1f).fillMaxWidth(),
					)
				}
			}

			MultiPlatformPager(
				platform = Platform.currentPlatform(),
				state = state.pagerState,
				modifier = Modifier.weight(1f).fillMaxHeight()
			) { page ->
				Column(Modifier.fillMaxSize()) {

					LessonDateBar(
						modifier = Modifier.fillMaxWidth().height(LessonTableDefaults.DateBarHeight),
						startDay = layout.startDay,
						week = page,
						dayCount = layout.dayCount,
						today = today,
						colors = colors,
					)

					LessonGrid(
						layout = layout,
						week = page,
						settings = settings,
						state = state,
						colors = colors,
						tableCoordinates = { tableCoordinates },
						onCellClick = onCellClick,
						onCellLongClick = onCellLongClick,
						modifier = Modifier.weight(1f).fillMaxWidth(),
					)
				}
			}
		}
	}
}
