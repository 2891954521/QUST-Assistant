package com.qust.helper.next.ui.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qust.helper.next.ui.component.Windows

/**
 * 有两个面板的响应式布局，根据窗口宽度自动调整面板排列方向
 * 
 * @param modifier 修饰符
 * @param first 第一个面板
 * @param second 第二个面板
 */
@Composable
fun TwoPane(
    modifier: Modifier = Modifier,
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit
) {
    when(Windows.windowsSize.widthSizeClass){
        WindowWidthSizeClass.Compact -> {
            Column(modifier = modifier) {
                first()
                second()
            }
        }

        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            Row(modifier = modifier) {
                Column(Modifier.weight(1f).fillMaxWidth(), content = first)
                Column(Modifier.weight(1f).fillMaxWidth(), content = second)
            }
        }
    }
}

/**
 * 有两个面板的响应式布局，根据窗口宽度自动调整面板排列方向
 *
 * @param modifier 修饰符
 * @param first 第一个面板
 * @param second 第二个面板
 */
@Composable
fun TwoPaneRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    first: @Composable RowScope.() -> Unit,
    second: @Composable RowScope.() -> Unit
) {
    if (Windows.isCompact) {
        Column(modifier = modifier) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = verticalAlignment, horizontalArrangement = horizontalArrangement, content = first)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = verticalAlignment, horizontalArrangement = horizontalArrangement, content = second)
        }
    } else {
        Row(modifier = modifier, verticalAlignment = verticalAlignment, horizontalArrangement = horizontalArrangement) {
            first()
            second()
        }
    }
}