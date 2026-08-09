package com.qust.helper.next.ui.component.overlay.toast

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

@Stable
data class ToastItem(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val type: ToastType,
    val icon: ImageVector? = null,
    val durationMillis: Long = 2000L
)