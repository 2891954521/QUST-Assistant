package com.qust.helper.next.ui.business.lesson.table.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.business.lesson.table.LessonCellInfo
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.business.lesson.table.model.LessonStatus
import com.qust.helper.next.ui.theme.Theme
import kotlin.math.roundToInt

@Composable
fun LessonCell(
	info: LessonCellInfo,
	hideTeacher: Boolean,
	colors: LessonTableColors,
	tableCoordinates: () -> LayoutCoordinates?,
	onClick: () -> Unit,
	onLongClick: (IntRect) -> Unit,
	modifier: Modifier = Modifier,
) {
	val displayed = info.displayed
	val lesson = displayed.entry.lesson
	val (background, textColor) = cellColors(displayed.status, lesson.colorLabel, colors)
	var cellCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

	Box(
		modifier = modifier
			.padding(1.dp)
			.clip(RoundedCornerShape(4.dp))
			.background(background)
			.onGloballyPositioned { cellCoordinates = it }
			.combinedClickable(
				onClick = onClick,
				onLongClick = {
					onLongClick(anchorInTable(cellCoordinates, tableCoordinates()))
				},
			),
	) {
		Column(
			modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			val nameStyle = if(displayed.status == LessonStatus.FINISHED) {
				TextStyle.Default.copy(color = textColor, textDecoration = TextDecoration.LineThrough)
			} else {
				TextStyle.Default.copy(color = textColor)
			}
			ProvideTextStyle(TextStyle.Default.copy(color = textColor, textAlign = TextAlign.Center)) {
				Text(
					text = lesson.name,
					style = nameStyle,
					textAlign = TextAlign.Center,
					maxLines = 3,
					overflow = TextOverflow.Ellipsis,
				)
				Text(
					text = lesson.place,
					textAlign = TextAlign.Center,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)
				if(!hideTeacher && lesson.teacher.isNotEmpty()) {
					Text(
						text = lesson.teacher,
						textAlign = TextAlign.Center,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
		if(info.candidates.size > 1) {
			Text(
				text = "${info.displayedIndex + 1}/${info.candidates.size}",
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(2.dp)
					.background(textColor.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
					.padding(horizontal = 4.dp, vertical = 1.dp),
				style = Theme.textStyles.caption,
				color = textColor.copy(alpha = 0.85f),
			)
		}
	}
}

private fun cellColors(status: LessonStatus, colorLabel: Int, colors: LessonTableColors): Pair<Color, Color> {
	return when(status) {
		LessonStatus.CURRENT -> {
			val background = colors.lessonBackground.colorAt(colorLabel)
			val text = colors.lessonText.colorAt(colorLabel)
			background to text
		}
		LessonStatus.UPCOMING -> colors.upcomingBackground to colors.upcomingText
		LessonStatus.FINISHED -> colors.finishedBackground to colors.finishedText
	}
}

private fun List<Color>.colorAt(label: Int): Color {
	if(isEmpty()) return Color.Gray
	return this[label.mod(size)]
}

private fun anchorInTable(cell: LayoutCoordinates?, table: LayoutCoordinates?): IntRect {
	if(cell == null || table == null || !cell.isAttached || !table.isAttached) return IntRect.Zero
	val topLeft = table.localPositionOf(cell, Offset.Zero)
	val bottomRight = table.localPositionOf(cell, Offset(cell.size.width.toFloat(), cell.size.height.toFloat()))
	return IntRect(
		left = topLeft.x.roundToInt(),
		top = topLeft.y.roundToInt(),
		right = bottomRight.x.roundToInt(),
		bottom = bottomRight.y.roundToInt(),
	)
}
