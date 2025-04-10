package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp


private val aBack = Color(0xFFE6F4FF)
private val aText = Color(0xFF1F9DD0)

private val bBack = Color(0xFFF5F5F5)
private val bText = Color(0xFF909090)

/**
 * 可拖动的批量选择器
 */
@Composable
fun LessonWeeksPicker(counts: Int, rows: Int, indexes: SnapshotStateList<Boolean>) {
	var width = 0
	var height = 0
	var hasChange = Array(10){ false }
	Box {
		Layout(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
			val col: Int = counts / rows + if(counts % rows == 0) 0 else 1
			width = coordinates.size.width / rows
			height = coordinates.size.height / col
		}.pointerInput(Unit) {
			detectDragGestures(
				onDragStart = { hasChange = Array(counts) { false } },
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
				measurable.measure(
					Constraints(
						minWidth = itemWidth,
						maxWidth = itemWidth,
						minHeight = 0,
						maxHeight = itemHeight
					)
				)
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
