package com.qust.helper.next.ui.component.overlay

import com.qust.helper.next.ui.component.overlay.toast.ToastAble
import com.qust.helper.next.ui.component.overlay.toast.ToastItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OverlayEventBus: ToastAble {

    private val _events = MutableSharedFlow<OverlayEvent>(
        extraBufferCapacity = 64
    )

    val events: SharedFlow<OverlayEvent> = _events.asSharedFlow()

    fun send(event: OverlayEvent) {
        _events.tryEmit(event)
    }

    suspend fun emit(event: OverlayEvent) {
        _events.emit(event)
    }

    override fun toast(toast: ToastItem) {
        send(OverlayEvent.Toast(toast))
    }

    fun showLoading(message: String) {
        send(OverlayEvent.Loading(true, message))
    }

    fun clearLoading() {
        send(OverlayEvent.Loading(false))
    }
}