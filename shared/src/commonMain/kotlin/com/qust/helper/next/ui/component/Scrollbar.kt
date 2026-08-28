package com.qust.helper.next.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 带垂直滚动条的 Column 组件
 */
@Composable
expect fun ColumnVerticalScroll(
	modifier: Modifier = Modifier,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	scrollBarSpace: Dp = 8.dp,
	content: @Composable ColumnScope.() -> Unit
)

/**
 * 带水平滚动条的 Column 组件
 */
@Composable
expect fun ColumnHorizontalScroll(modifier: Modifier = Modifier, scrollBarSpace: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit)

/**
 * 带垂直滚动条的 Row 组件
 */
@Composable
expect fun RowVerticalScroll(modifier: Modifier = Modifier, scrollBarSpace: Dp = 8.dp, content: @Composable RowScope.() -> Unit)

/**
 * 带水平滚动条的 Row 组件
 */
@Composable
expect fun RowHorizontalScroll(modifier: Modifier = Modifier, scrollBarSpace: Dp = 8.dp, content: @Composable RowScope.() -> Unit)

/**
 * 带垂直滚动条的 LazyColumn 组件
 */
@Composable
expect fun LazyColumnVerticalScroll(modifier: Modifier = Modifier, scrollBarSpace: Dp = 8.dp, content: LazyListScope.() -> Unit)

/**
 * 带水平滚动条的 LazyRow 组件
 */
@Composable
expect fun LazyRowHorizontalScroll(modifier: Modifier = Modifier, scrollBarSpace: Dp = 8.dp, content: LazyListScope.() -> Unit)

/**
 * 带垂直滚动条的 LazyVerticalGrid 组件
 */
@Composable
expect fun LazyVerticalGridVerticalScroll(
	modifier: Modifier = Modifier,
	columns: GridCells,
	scrollBarSpace: Dp = 8.dp,
	content: LazyGridScope.() -> Unit
)