package com.qust.helper.ui.widget.lesson.lessonEdit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 颜色单选器
 */
@Composable
fun LessonColorPicker(colors: Array<Color>, select: Int, rows: Int, onClick: (Int) -> Unit = { }){
	var selectedIndex by remember { mutableIntStateOf(select) }
	Box {
		Layout(modifier = Modifier.fillMaxWidth(), content = {
			repeat(colors.size) { index ->
				val iconValue = remember { Animatable(0F) }
				val scaleValue = remember { Animatable(1f) }
				LaunchedEffect(selectedIndex) {
					if(selectedIndex == index){
						launch {
							scaleValue.animateTo(targetValue = 0.8F, animationSpec = tween(durationMillis = 100, easing = EaseInOut))
							scaleValue.animateTo(targetValue = 1F, animationSpec = tween(durationMillis = 200, easing = EaseInOut))
						}
						iconValue.animateTo(targetValue = 1F, animationSpec = tween(durationMillis = 100, easing = EaseInOut))
					}else{
						if(iconValue.value == 1F){
							iconValue.animateTo(targetValue = 0F, animationSpec = tween(durationMillis = 200, easing = EaseInOut))
						}
					}
				}
				Box(modifier = Modifier.fillMaxSize().scale(scaleValue.value).pointerInput(Unit){
					detectTapGestures(onTap = { selectedIndex = index; onClick(index) })
				}) {
					Box(modifier = Modifier.fillMaxSize().padding(4.dp).background(colors[index], CircleShape)){
						Icon(
							imageVector = Icons.Rounded.Check,
							contentDescription = null,
							modifier = Modifier.align(Alignment.Center).padding(4.dp).scale(iconValue.value),
							tint = Color.DarkGray
						)
					}
				}
			}
		}) { measurables, constraints ->
			val itemWidth = constraints.maxWidth / rows
			val placeAbles = measurables.map { measurable ->
				measurable.measure(Constraints(minWidth = itemWidth, maxWidth = itemWidth, minHeight = itemWidth, maxHeight = itemWidth))
			}
			val totalHeight = (placeAbles.size / rows + if(placeAbles.size % rows == 0) 0 else 1) * itemWidth
			layout(constraints.maxWidth, totalHeight) {
				var xPos = 0
				var yPos = 0
				for(placeable in placeAbles) {
					placeable.place(xPos, yPos)
					xPos += itemWidth
					if(xPos >= constraints.maxWidth) {
						xPos = 0
						yPos += itemWidth
					}
				}
			}
		}
	}
}
