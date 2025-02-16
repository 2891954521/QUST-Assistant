package com.qust.helper.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 各种选择器
 */
object Picker {

	private val aBack = Color(0xFFE6F4FF)
	private val aText = Color(0xFF1F9DD0)

	private val bBack = Color(0xFFF5F5F5)
	private val bText = Color(0xFF909090)

	/**
	 * 颜色单选器
	 */
	@Composable
	fun ColorPicker(colors: Array<Color>, select: Int, rows: Int, onClick: (Int) -> Unit = { }){
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

	/**
	 * 可拖动的批量选择器
	 */
	@Composable
	fun DragAblePicker(counts: Int, rows: Int, indexes: SnapshotStateList<Boolean> = remember { List(counts){ false }.toMutableStateList() }) {
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

	/**
	 * 有动画的数字选择器
	 */
	@Composable
	fun AnimNumberPicker(modifier: Modifier = Modifier, number: Int, onAdd: () -> Unit = { }, onSub: () -> Unit = { }) {
		Row(
			modifier = modifier.padding(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(modifier = Modifier.clip(CircleShape).clickable { onSub() }){
				Icon(
					contentDescription = null,
					imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
					modifier = Modifier.padding(8.dp)
				)
			}
			AnimatedContent(targetState = number, label = "", transitionSpec = {
				if(targetState > initialState) {
					(slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
				} else {
					(slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
				}.using(SizeTransform(clip = false))
			}) { targetCount ->
				Text(text = targetCount.toString(), modifier = Modifier.padding(8.dp))
			}
			Box(modifier = Modifier.clip(CircleShape).clickable { onAdd() }){
				Icon(
					contentDescription = null,
					imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
					modifier = Modifier.padding(8.dp)
				)
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
}