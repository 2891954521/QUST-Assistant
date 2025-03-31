package com.qust.helper.ui.widget.toast

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@Stable
class ToastUIState {
	private val mutex = Mutex()

	var currentData: ToastData? by mutableStateOf(null)
		private set

	private var continuation: CancellableContinuation<Unit>? = null

	suspend fun show(toastData: ToastData): Unit = mutex.withLock {
		try {
			suspendCancellableCoroutine { continuation ->
				this.continuation = continuation
				currentData = toastData
			}
		} finally {
			currentData = null
		}
	}

	suspend fun run() {
		delay(2000L)
		continuation?.resume(Unit)
	}
}