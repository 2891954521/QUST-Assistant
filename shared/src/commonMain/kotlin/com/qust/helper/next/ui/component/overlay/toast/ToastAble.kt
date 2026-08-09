package com.qust.helper.next.ui.component.overlay.toast

interface ToastAble {

    fun toast(message: String)        = toast(ToastItem(message = message, type = ToastType.NORMAL))

    fun toastNormal(message: String)  = toast(ToastItem(message = message, type = ToastType.NORMAL))

    fun toastSuccess(message: String) = toast(ToastItem(message = message, type = ToastType.SUCCESS))

    fun toastInfo(message: String)    = toast(ToastItem(message = message, type = ToastType.INFO))

    fun toastWarning(message: String) = toast(ToastItem(message = message, type = ToastType.WARNING))

    fun toastError(message: String)   = toast(ToastItem(message = message, type = ToastType.ERROR))

    fun toast(toast: ToastItem)
}