package com.qust.helper.next.ui.router

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

class DesktopPageResultStore {

    private val results = MutableStateFlow<Map<PageResultKey, PageResult>>(emptyMap())

    @Suppress("UNCHECKED_CAST")
    fun observe(key: PageResultKey): Flow<PageResult> = results.mapNotNull { it[key] }.distinctUntilChanged()

    internal fun publish(key: PageResultKey, result: PageResult) = results.update { it + (key to result) }

    fun consume(key: PageResultKey) = results.update { it - key }

    fun clear() {
        results.value = emptyMap()
    }
}