package com.qust.helper.next.ui.component.list

import com.qust.helper.next.ui.viewmodel.AsyncAble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


abstract class AbsListAdapter<T>(
    val async: AsyncAble
) {

    abstract val _uiState: MutableStateFlow<ListUIState<T>>

    abstract val uiState: StateFlow<ListUIState<T>>

    /**
     * 当前页数
     */
    var currentPage = 0

    protected var items = mutableListOf<T>()

    private val mutex = Mutex()

    /**
     * 刷新
     */
    fun refresh(){
        if (!beforeRefresh()) return
        async.runInBackground {
            mutex.withLock {
                _uiState.update { it.copy(isRefreshing = true, isEndReached = false) }

                currentPage = 1

                try {
                    onRefresh()
                } catch(e: Throwable) {
                    _uiState.update { state -> state.copy(isRefreshing = false, isEndReached = true) }
                    throw e
                }
            }
        }
    }

    /**
     * 加载更多
     */
    fun loadMore(){
        if (_uiState.value.isLoadingMore || _uiState.value.isEndReached) return
        async.runInBackground {
            mutex.withLock {
                if (_uiState.value.isLoadingMore || _uiState.value.isEndReached) return@withLock

                _uiState.update { it.copy(isLoadingMore = true) }

                currentPage++

                try {
                    onLoad(currentPage)
                } catch(e: Throwable) {
                    _uiState.update { state -> state.copy(isRefreshing = false, isEndReached = true) }
                    throw e
                }
            }
        }
    }

    /**
     * 更新指定位置的Item
     *
     * 禁止在循环中多次调用该函数，需要批量更新请使用 [AbsListAdapter.update]
     */
    fun update(index: Int, newItem: T) {
        items = items.toMutableList().apply { this[index] = newItem }
        _uiState.update { state -> state.copy(items = items) }
    }

    /**
     * 通过遍历批量更新 Item
     *
     * @param map 更新后的 Item，返还 null 表示不更新当前 Item
     */
    fun update(map: (Int, T) -> T?) {
        async.runInBackground {
            mutex.withLock {
                items = MutableList(items.size){ index ->
                    val it = items[index]
                    map(index, it) ?: it
                }
                _uiState.update { state -> state.copy(items = items) }
            }
        }
    }

    /**
     * 删除指定位置的Item
     */
    fun removeAt(index: Int) {
        items = items.toMutableList().apply { removeAt(index) }
        _uiState.update { state -> state.copy(items = items) }
    }

    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * 刷新前调用
     * @return 是否允许刷新
     */
    open fun beforeRefresh(): Boolean = true


    protected abstract suspend fun onRefresh()

    protected abstract suspend fun onLoad(page: Int)


    operator fun get(index: Int): T = items[index]

    operator fun set(index: Int, item: T) = update(index, item)
}