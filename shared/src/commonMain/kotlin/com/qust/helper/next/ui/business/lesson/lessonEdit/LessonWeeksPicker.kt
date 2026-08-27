package com.qust.helper.next.ui.business.lesson.lessonEdit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.component.LocalScale
import com.qust.helper.next.ui.component.Windows
import com.qust.helper.next.ui.theme.Theme


private val aBack = Color(0xFFE6F4FF)
private val aText = Color(0xFF1F9DD0)

private val bBack = Color(0xFFF5F5F5)
private val bText = Color(0xFF909090)


/**
 * 可拖动的批量选择器
 */
@Composable
fun LessonWeeksPicker(counts: Int, itemWidth: Dp, indexes: SnapshotStateList<Boolean>) {
	val widthDp = Windows.windowsDpSize.width

	var width by remember {  mutableIntStateOf(0) }
	var height by remember { mutableIntStateOf(0) }

	var rows by remember { mutableIntStateOf(6) }
	val hasChange = remember(counts) { MutableList(counts){ false } }

	Box {
		Layout(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
			rows = (widthDp / itemWidth).toInt().coerceIn(8, 16)
			val col = (counts / rows) + (if(counts % rows == 0) 0 else 1)
			width = coordinates.size.width / rows
			height = coordinates.size.height / col
		}.pointerInput(Unit) {
			detectDragGestures(
				onDragStart = { hasChange.fill(false) },
				onDrag = { change, _ ->
					val x = (change.position.x / width).toInt()
					if(x < rows){
						val down: Int = (change.position.y / height).toInt() * rows + x
						if(-1 < down && down < hasChange.size) {
							if(!hasChange[down]) {
								hasChange[down] = true
								indexes[down] = !indexes[down]
							}
						}
					}
				}
			)
		}, content = {
			repeat(counts) { index ->
				AnimatedItem(isSelect = indexes[index], onClick = { indexes[index] = !indexes[index] }){
					Box(modifier = Modifier.fillMaxSize().padding(4.dp)
						.background(if(indexes[index]) aBack else bBack, RoundedCornerShape(8.dp))
					){
						Text(
							text = (index + 1).toString(),
							color = if(indexes[index]) aText else bText,
							textAlign = TextAlign.Center,
							modifier = Modifier.align(Alignment.Center)
						)
					}
				}
			}
		}) { measurables, constraints ->
			val itemWidth = (constraints.maxWidth / rows)
			val itemHeight = itemWidth * 2 / 3
			val placeAbles = measurables.map { measurable ->
				measurable.measure(Constraints(minWidth = itemWidth, maxWidth = itemWidth, minHeight = 0, maxHeight = itemHeight))
			}
			val totalHeight = (placeAbles.size / rows + if(placeAbles.size % rows == 0) 0 else 1) * itemHeight
			layout(constraints.maxWidth, totalHeight) {
				var index = 0
				var xPos = 0
				var yPos = 0
				for(placeable in placeAbles) {
					placeable.place(xPos, yPos)
					xPos += itemWidth
					index++
					if(index == rows) {
						index = 0
						xPos = 0
						yPos += itemHeight
					}
				}
			}
		}
	}
}

@Composable
private fun AnimatedItem(isSelect: Boolean, onClick: () -> Unit = { }, content: @Composable () -> Unit) {
	val scaleValue = remember { Animatable(1f) }
	LaunchedEffect(isSelect) {
		scaleValue.animateTo(targetValue = 0.8F, animationSpec = tween(durationMillis = 100))
		scaleValue.animateTo(targetValue = 1F, animationSpec = tween(durationMillis = 100))
	}
	Box(modifier = Modifier.fillMaxSize().scale(scaleValue.value).pointerInput(Unit){
		detectTapGestures(onTap = { onClick() })
	}) {
		content()
	}
}
