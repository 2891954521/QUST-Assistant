package com.qust.helper.ui.widget.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BottomDialog(
	isExpanded: Boolean,
	content: @Composable () -> Unit
) {
	val density = LocalDensity.current
	var screenHeight by remember { mutableStateOf(0.dp) }

	Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
		with(density) {
			screenHeight = coordinates.size.height.toDp()
		}
	}) {
		BottomSlideComponent(
			isExpanded = isExpanded,
			screenHeight = screenHeight,
			content = content
		)
	}
}

@Composable
fun BottomSlideComponent(
	isExpanded: Boolean,
	screenHeight: Dp,
	content: @Composable () -> Unit
) {
	var visibility by remember { mutableStateOf(false) }

	val animationProgress by animateFloatAsState(
		targetValue = if(isExpanded){
			visibility = true
			1f
		} else 0f,
		animationSpec = tween(durationMillis = 500),
		label = "slide_animation"
	){
		if(!isExpanded) visibility = false
	}

	if(!visibility) return

	val offset = screenHeight - screenHeight * animationProgress

	if(animationProgress < 0.99f) {
		Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f * (1 - animationProgress)),))
	}

	Box(modifier = Modifier.fillMaxWidth().height(screenHeight).offset(y = offset).background(MaterialTheme.colorScheme.surface)) {
		content()
	}
}