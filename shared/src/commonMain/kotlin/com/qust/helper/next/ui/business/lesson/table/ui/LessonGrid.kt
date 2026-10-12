package com.qust.helper.next.ui.business.lesson.table.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntRect
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.business.lesson.table.axis.offsetToPx
import com.qust.helper.next.ui.business.lesson.table.model.LessonTableLayout
import com.qust.helper.next.ui.business.lesson.table.state.LessonTableState

@Composable
fun LessonGrid(
	layout: LessonTableLayout,
	week: Int,
	settings: LessonTableRepository.LessonTableSetting,
	state: LessonTableState,
	colors: LessonTableColors,
	tableCoordinates: () -> LayoutCoordinates?,
	onCellClick: (LessonCellInfo) -> Unit,
	onCellLongClick: (LessonCellInfo, IntRect) -> Unit,
	modifier: Modifier = Modifier,
) {
	Layout(
		modifier = modifier,
		content = {
			layout.groups.forEach { group ->
				val candidates = group.resolve(week, settings)
				if(candidates.isEmpty()) return@forEach
				val info = LessonCellInfo(
					group = group,
					week = week,
					candidates = candidates,
					displayedIndex = state.displayedIndex(group, candidates),
				)
				LessonCell(
					info = info,
					hideTeacher = settings.hideTeacher,
					colors = colors,
					tableCoordinates = tableCoordinates,
					onClick = { onCellClick(state.cycle(info)) },
					onLongClick = { anchor -> onCellLongClick(info, anchor) },
					modifier = Modifier.lessonCellInfo(info),
				)
			}
		}
	) { measurables, constraints ->
		val width = constraints.maxWidth
		val height = constraints.maxHeight
		val dayCount = layout.dayCount.coerceAtLeast(1)
		val columnWidth = width / dayCount
		val placeables = measurables.map { measurable ->
			val info = measurable.parentData as LessonCellParentData
			val entry = info.info.displayed.entry
			val top = layout.axis.offsetToPx(entry.startOffset, height)
			val bottom = layout.axis.offsetToPx(entry.endOffset, height)
			val cellHeight = (bottom - top).coerceAtLeast(0)
			val placeable = measurable.measure(
				Constraints(
					minWidth = columnWidth,
					maxWidth = columnWidth,
					minHeight = cellHeight,
					maxHeight = cellHeight,
				)
			)
			Triple(placeable, info.info.group.dayOfWeek * columnWidth, top)
		}
		layout(width, height) {
			placeables.forEach { (placeable, x, y) ->
				placeable.place(x, y)
			}
		}
	}
}

private class LessonCellParentData(val info: LessonCellInfo) : ParentDataModifier {
	override fun Density.modifyParentData(parentData: Any?): Any = this@LessonCellParentData
}

private fun Modifier.lessonCellInfo(info: LessonCellInfo): Modifier = this.then(LessonCellParentData(info))
