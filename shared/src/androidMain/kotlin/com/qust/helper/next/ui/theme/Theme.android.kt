package com.qust.helper.next.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual inline fun PlatformAppTheme(content: @Composable (() -> Unit)) {
    content()
}