package com.qust.helper.next.ui.component.overlay.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import java.util.UUID

/**
 * @param id 对话框ID
 * @param dismissOnMaskClick 是否在点击遮罩层时关闭对话框
 * @param onDismiss 对话框关闭后回调
 * @param content 对话框内容
 */
@Stable
data class DialogItem(
    val id: String = UUID.randomUUID().toString(),
    val dismissOnMaskClick: Boolean = true,
    val onDismiss: () -> Unit,
    val content: @Composable () -> Unit,
)