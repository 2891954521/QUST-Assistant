package com.qust.helper.next.ui.component.list

import androidx.compose.runtime.Immutable

/**
 * 列表UI状态
 * @param isRefreshing 是否正在刷新
 * @param isLoadingMore 是否正在加载更多
 * @param isEndReached 是否已到达末尾，此状态表示列表已加载完所有数据
 * @param items 列表数据
 */
@Immutable
data class ListUIState<T>(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isEndReached: Boolean = false,
    val items: List<T> = emptyList()
)
