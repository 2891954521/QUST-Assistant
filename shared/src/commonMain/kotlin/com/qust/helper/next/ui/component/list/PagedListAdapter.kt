package com.qust.helper.next.ui.component.list

import com.qust.helper.next.ui.viewmodel.AsyncAble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 支持下拉刷新和多页加载的列表Adapter
 *
 * @param defaultPageSize 每页加载数量
 * @param loadMoreFunc 加载更多的函数，若不传入此函数时必须重写 loadMore 函数
 */
open class PagedListAdapter<T>(
    async: AsyncAble,
    val defaultPageSize: Int = 20,
    val loadMoreFunc: (suspend (page: Int, pageSize: Int) -> List<T>)? = null
): AbsListAdapter<T>(async), Iterable<T> {

    override val _uiState = MutableStateFlow(ListUIState<T>())

    override val uiState: StateFlow<ListUIState<T>> = _uiState.asStateFlow()

    open suspend fun loadMore(page: Int): Pair<List<T>, Boolean> {
        if(loadMoreFunc != null){
            val list = loadMoreFunc(page, defaultPageSize)
            return list to (list.size < defaultPageSize)
        }else{
            return emptyList<T>() to true
        }
    }

    override suspend fun onRefresh(){
        val (items, isEnd) = loadMore(currentPage)
        this.items = items.toMutableList()
        _uiState.update { state ->
            state.copy(
                isRefreshing = false,
                items = this.items,
                isEndReached = isEnd
            )
        }
    }

    override suspend fun onLoad(page: Int){
        val (items, isEnd) = loadMore(page)
        this.items += items
        _uiState.update { state ->
            state.copy(
                isLoadingMore = false,
                items = this.items,
                isEndReached = isEnd
            )
        }
    }

    override operator fun iterator(): Iterator<T> = items.iterator()
}
