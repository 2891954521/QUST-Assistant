package com.qust.helper.next.ui.business.lesson.table.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.lesson.Lesson
import com.qust.helper.next.entity.lesson.TimeTable
import com.qust.helper.next.ui.business.lesson.LessonTableDefaults
import com.qust.helper.next.ui.business.lesson.table.LessonTableColors
import com.qust.helper.next.ui.business.lesson.table.axis.LessonTimeAxis
import com.qust.helper.next.ui.business.lesson.table.axis.SlotTimeAxis
import com.qust.helper.next.ui.business.lesson.table.axis.TimeSegment
import com.qust.helper.next.ui.business.lesson.table.axis.formatMinuteOfDay
import com.qust.helper.next.ui.business.lesson.table.axis.toPx
import com.qust.helper.next.ui.theme.Theme


@Preview
@Composable
private fun LessonTimeBarPreview(){
	val lesson = remember { listOf<Lesson>() }
	val timeTable = remember { TimeTable.DEFAULT }
	val axis = remember(lesson) { SlotTimeAxis.create(timeTable, lesson) }

	LessonTimeBar(axis, LessonTableDefaults.colors(), modifier = Modifier.width(64.dp))
}

@Composable
fun LessonTimeBar(
	axis: LessonTimeAxis,
	colors: LessonTableColors,
	modifier: Modifier = Modifier,
) {
	val visible = remember(axis) { axis.segments.filter { it.weight > 0f } }
	val measurePolicy = remember(axis, visible) { LessonTimeBarMeasurePolicy(axis, visible) }
	Layout(
		modifier = modifier,
		measurePolicy = measurePolicy,
		content = {
			visible.forEach { segment ->
				when(segment.kind) {
					TimeSegment.Kind.SLOT -> SlotSegmentLabel(segment, colors)
					TimeSegment.Kind.GAP -> GapSegmentLabel(segment, colors)
					TimeSegment.Kind.EXTRA -> ExtraSegmentLabel(segment, colors)
				}
			}
		},
	)
}

private class LessonTimeBarMeasurePolicy(
	private val axis: LessonTimeAxis,
	private val segments: List<TimeSegment>,
) : MeasurePolicy {

	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		val height = constraints.maxHeight
		val contentWidth = measurables.maxOfOrNull { it.minIntrinsicWidth(height) } ?: 0
		val width = constraints.constrainWidth(contentWidth)
		val placeables = measurables.mapIndexed { index, measurable ->
			val segment = segments[index]
			val top = axis.toPx(segment.startMinute, height)
			val bottom = axis.toPx(segment.endMinute, height)
			val childHeight = (bottom - top).coerceAtLeast(0)
			val placeable = measurable.measure(
				Constraints(minWidth = width, maxWidth = width, minHeight = childHeight, maxHeight = childHeight)
			)
			placeable to top
		}
		return layout(width, height) {
			placeables.forEach { (placeable, top) ->
				placeable.place(0, top)
			}
		}
	}

	override fun IntrinsicMeasureScope.minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = measurables.maxOfOrNull { it.minIntrinsicWidth(height) } ?: 0

	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = minIntrinsicWidth(measurables, height)
}

@Composable
private fun SlotSegmentLabel(segment: TimeSegment, colors: LessonTableColors) {
	Layout(
		modifier = Modifier.padding(horizontal = 4.dp),
		measurePolicy = SlotSegmentLabelMeasurePolicy,
		content = {
			Text(
				text = (segment.slotIndex + 1).toString(),
				style = Theme.textStyles.bodyStrong,
				color = colors.timeBarText,
				textAlign = TextAlign.Center,
			)
			Text(
				text = formatMinuteOfDay(segment.startMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
			Text(
				text = formatMinuteOfDay(segment.endMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
		},
	)
}

private object SlotSegmentLabelMeasurePolicy : MeasurePolicy {

	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		val loose = constraints.copy(minWidth = 0, minHeight = 0)
		val index = measurables[0].measure(loose)
		val start = measurables[1].measure(loose)
		val end = measurables[2].measure(loose)
		val showEnd = constraints.maxHeight >= index.height + start.height + end.height
		val showStart = constraints.maxHeight >= index.height + start.height
		val visible = buildList {
			add(index)
			if(showStart) add(start)
			if(showEnd) add(end)
		}
		val contentHeight = visible.sumOf { it.height }
		val width = constraints.constrainWidth(visible.maxOf { it.width })
		val height = constraints.constrainHeight(contentHeight)
		return layout(width, height) {
			var y = ((height - contentHeight) / 2).coerceAtLeast(0)
			visible.forEach { placeable ->
				placeable.place((width - placeable.width) / 2, y)
				y += placeable.height
			}
		}
	}

	override fun IntrinsicMeasureScope.minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)

	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)
}

@Composable
private fun GapSegmentLabel(segment: TimeSegment, colors: LessonTableColors) {
	val captionPx = with(LocalDensity.current) { Theme.textStyles.caption.lineHeight.roundToPx() }
	val dash = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) }
	val measurePolicy = remember(captionPx) { GapSegmentLabelMeasurePolicy(captionPx) }
	Layout(
		modifier = Modifier
			.padding(horizontal = 4.dp)
			.drawBehind {
				val y = size.height / 2f
				drawLine(
					color = colors.gapLine,
					start = Offset(0f, y),
					end = Offset(size.width, y),
					strokeWidth = 1.dp.toPx(),
					pathEffect = dash,
				)
			},
		measurePolicy = measurePolicy,
		content = {
			Text(
				text = formatMinuteOfDay(segment.startMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
			Text(
				text = formatMinuteOfDay(segment.endMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
		},
	)
}

private class GapSegmentLabelMeasurePolicy(
	private val captionPx: Int,
) : MeasurePolicy {

	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		val showTimes = constraints.maxHeight >= captionPx * 2
		val loose = constraints.copy(minWidth = 0, minHeight = 0)
		val start = measurables[0].measure(loose)
		val end = measurables[1].measure(loose)
		val width = constraints.constrainWidth(maxOf(start.width, end.width))
		val height = constraints.constrainHeight(start.height + end.height)
		return layout(width, height) {
			if(showTimes) {
				start.place((width - start.width) / 2, 0)
				end.place((width - end.width) / 2, (height - end.height).coerceAtLeast(0))
			}
		}
	}

	override fun IntrinsicMeasureScope.minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)

	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)
}

@Composable
private fun ExtraSegmentLabel(segment: TimeSegment, colors: LessonTableColors) {
	Layout(
		modifier = Modifier.padding(horizontal = 4.dp),
		measurePolicy = ExtraSegmentLabelMeasurePolicy,
		content = {
			Text(
				text = formatMinuteOfDay(segment.startMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
			Text(
				text = formatMinuteOfDay(segment.endMinute),
				style = Theme.textStyles.caption,
				color = colors.timeBarSecondaryText,
				textAlign = TextAlign.Center,
				maxLines = 1,
				softWrap = false,
			)
		},
	)
}

private object ExtraSegmentLabelMeasurePolicy : MeasurePolicy {

	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints,
	): MeasureResult {
		val loose = constraints.copy(minWidth = 0, minHeight = 0)
		val start = measurables[0].measure(loose)
		val end = measurables[1].measure(loose)
		val width = constraints.constrainWidth(maxOf(start.width, end.width))
		val height = constraints.constrainHeight(start.height + end.height)
		return layout(width, height) {
			start.place((width - start.width) / 2, 0)
			end.place((width - end.width) / 2, (height - end.height).coerceAtLeast(0))
		}
	}

	override fun IntrinsicMeasureScope.minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)

	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int,
	): Int = timeLabelIntrinsicWidth(measurables, height)
}

private fun IntrinsicMeasureScope.timeLabelIntrinsicWidth(
	measurables: List<IntrinsicMeasurable>,
	height: Int,
): Int = measurables.maxOfOrNull { it.minIntrinsicWidth(height) } ?: 0
