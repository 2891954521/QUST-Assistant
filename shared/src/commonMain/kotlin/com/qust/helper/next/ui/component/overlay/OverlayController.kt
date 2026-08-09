package com.qust.helper.next.ui.component.overlay

import androidx.compose.runtime.*
import com.qust.helper.next.ui.component.overlay.dialog.DialogItem
import com.qust.helper.next.ui.component.overlay.toast.ToastAble
import com.qust.helper.next.ui.component.overlay.toast.ToastItem
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

val LocalToast = staticCompositionLocalOf<ToastAble> {
    error("LocalToast not provided")
}

val LocalOverlayController = staticCompositionLocalOf<OverlayController> {
    error("OverlayController not provided")
}


class OverlayController(
    val scope: CoroutineScope
): ToastAble {

    // =================================================================
    // Dialog
    // =================================================================

    private val _dialogs = mutableStateListOf<DialogItem>()

    internal val dialogs: List<DialogItem> get() = _dialogs

    fun showDialog(
        id: String,
        dismissOnMaskClick: Boolean = true,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit
    ) {
        val index = _dialogs.indexOfFirst { it.id == id }
        val item = DialogItem(
            id = id,
            dismissOnMaskClick = dismissOnMaskClick,
            onDismiss = onDismiss,
            content = content
        )

        if (index >= 0) {
            _dialogs[index] = item
        } else {
            _dialogs.add(item)
        }
    }

    fun dismissDialog() {
        val dialog = _dialogs.removeLastOrNull()
        dialog?.onDismiss()
    }

    fun dismissDialog(id: String) {
        _dialogs.firstOrNull { it.id == id }?.let {
            _dialogs.remove(it)
            it.onDismiss()
        }
    }

    // =================================================================
    // Loading
    // =================================================================

    private var loadingCount by mutableIntStateOf(0)

    private val loadingMutex = Mutex()

    internal val loadingVisible: Boolean get() = loadingCount > 0

    internal var loadingText by mutableStateOf("")
        private set

    fun showLoading(text: String) = scope.launch { showLoadingSync(text) }

    suspend fun showLoadingSync(text: String) {
        loadingMutex.withLock {
            loadingCount++
            loadingText = text
        }
    }

    fun dismissLoading() = scope.launch { dismissLoadingSync() }

    suspend fun dismissLoadingSync() {
        loadingMutex.withLock {
            loadingCount = (loadingCount - 1).coerceAtLeast(0)
            loadingText = ""
        }
    }

    // =================================================================
    // Toast
    // =================================================================

    internal var toastData: ToastItem? by mutableStateOf(null)

    private val toastMutex = Mutex()

    private var continuation: CancellableContinuation<Unit>? = null

    override fun toast(toast: ToastItem) {
        scope.launch {
            toastMutex.withLock {
                try {
                    withTimeoutOrNull(toast.durationMillis + 1_000L) {
                        suspendCancellableCoroutine { continuation ->
                            this@OverlayController.continuation = continuation
                            this@OverlayController.toastData = toast
                        }
                    }
                } finally {
                    this@OverlayController.toastData = null
                }
            }
        }
    }

    /**
     * Toast 被 UI 渲染出来时的回调
     */
    suspend fun onShowToast(toastData: ToastItem){
        delay(toastData.durationMillis)
        continuation?.resume(Unit)
    }
}