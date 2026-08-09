package com.qust.helper.next.ui.component.list

import com.qust.helper.next.ui.viewmodel.AsyncAble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 支持下拉刷新的列表Adapter
 *
 * 内容不分页, 通过loadMoreFunc一次性加载
 */
open class SinglePageAdapter<T>(
    async: AsyncAble,
    val loadMoreFunc: suspend () -> List<T>
): AbsListAdapter<T>(async), Iterable<T> {

    override val _uiState = MutableStateFlow(ListUIState<T>())

    override val uiState: StateFlow<ListUIState<T>> = _uiState.asStateFlow()

    override suspend fun onRefresh(){
        val items = loadMoreFunc()
        this.items = items.toMutableList()
        _uiState.update { state ->
            state.copy(
                isRefreshing = false,
                items = this.items,
                isEndReached = true
            )
        }
    }

    override suspend fun onLoad(page: Int){
        val items = loadMoreFunc()
        this.items = items.toMutableList()
        _uiState.update { state ->
            state.copy(
                isLoadingMore = false,
                items = this.items,
                isEndReached = true
            )
        }
    }

    override operator fun iterator(): Iterator<T> = items.iterator()
}
