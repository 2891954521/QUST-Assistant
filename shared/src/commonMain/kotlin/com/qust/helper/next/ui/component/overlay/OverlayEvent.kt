package com.qust.helper.next.ui.component.overlay

import com.qust.helper.next.ui.component.overlay.toast.ToastItem

sealed interface OverlayEvent {

    data class Toast(val data: ToastItem) : OverlayEvent

    data class Loading(val visible: Boolean, val text: String? = null) : OverlayEvent
}