package com.qust.helper.next.ui.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.qust.helper.next.common.Platform


@Composable
fun MultiPlatformPager(
	platform: Platform.PlatformType,
	state: PagerState,
	modifier: Modifier = Modifier,
	content: @Composable PagerScope.(page:Int) -> Unit,
) {
	if(platform == Platform.PlatformType.ANDROID) {
		HorizontalPager(state = state, modifier = modifier, pageContent = content)
	} else {
		VerticalPager(state = state, modifier = modifier, pageContent = content)
	}
}

@Composable
fun MultiPlatformPager(
	orientation: Orientation,
	state: PagerState,
	modifier: Modifier = Modifier,
	content: @Composable PagerScope.(page:Int) -> Unit,
) {
	if(orientation == Orientation.Horizontal) {
		HorizontalPager(state = state, modifier = modifier, pageContent = content)
	} else {
		VerticalPager(state = state, modifier = modifier, pageContent = content)
	}
}