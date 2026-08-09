package com.qust.helper.next.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qust.helper.next.ui.component.overlay.OverlayEventBus
import com.qust.helper.next.ui.component.overlay.toast.ToastAble
import com.qust.helper.next.ui.router.params.EmptyPageParam
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.viewmodel.AsyncAble
import com.qust.helper.next.ui.viewmodel.AsyncAbleImpl
import kotlinx.coroutines.CoroutineScope

abstract class BaseViewModel(
    val overlay: OverlayEventBus = OverlayEventBus()
): ViewModel(), ToastAble by overlay, AsyncAble {

    val async: AsyncAbleImpl = AsyncAbleImpl(viewModelScope, overlay)

    protected var params: PageParam = EmptyPageParam

    open fun onCreate(param: PageParam) {
        this.params = param
    }

    override fun runInBackground(block: suspend CoroutineScope.() -> Unit) = async.runInBackground(block)

    override fun loading(msg: String, block: suspend CoroutineScope.() -> Unit) = async.loading(msg, block)

    override suspend fun <T> loadingSuspend(msg: String, block: suspend () -> T?): T? = async.loadingSuspend(msg, block)

    override suspend fun <T> loadingSuspend(msg: String, default: T, block: suspend () -> T): T = async.loadingSuspend(msg, default, block)

    override suspend fun <T> loadingSuspend(msg: String, onError: (Throwable) -> Pair<Boolean, T>, block: suspend () -> T): T = async.loadingSuspend(msg, onError, block)

    override fun handleException(catchBy: String?, e: Throwable) = async.handleException(catchBy, e)
}