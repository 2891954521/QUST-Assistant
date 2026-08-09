package com.qust.helper.next.ui.component.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 支持下拉刷新和加载更多的列表组件
 *
 * @param rowCount 一行显示几个Item，大于1时每个Item均分水平空间
 * @param reachEndContent 拉到低时显示的内容
 * @param itemContent 单个Item的内容
 */
@Composable
fun <T> SuperListUI(
    adapter: AbsListAdapter<T>,
    modifier: Modifier = Modifier,
    rowCount: Int = 1,
    reachEndContent: (@Composable LazyGridItemScope.() -> Unit)? = null,
    itemContent: (@Composable LazyGridItemScope.(index: Int, item: T) -> Unit)
) {
    val ui by adapter.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyGridState()

    // 检测滑动到底部
    val shouldLoadMore by remember(listState) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisible.index >= listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if(shouldLoadMore && !ui.isLoadingMore && !ui.isEndReached) {
            adapter.loadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = ui.isRefreshing,
        onRefresh = adapter::refresh,
        modifier = modifier
    ) {
        LazyVerticalGrid(GridCells.Fixed(rowCount), Modifier.fillMaxSize(), state = listState) {
            items(ui.items.size) { index ->
                val item = ui.items[index]
                itemContent(index, item)
            }

            if(ui.isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if(reachEndContent != null && ui.isEndReached) {
                item(span = { GridItemSpan(maxLineSpan) }, content = reachEndContent)
            }
        }
    }
}