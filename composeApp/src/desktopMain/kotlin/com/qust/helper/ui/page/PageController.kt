package com.qust.helper.ui.page

import androidx.compose.runtime.Composable
import com.qust.helper.viewmodel.ApplicationViewModel

@Composable
actual fun rememberPageController(): PageController {
    return ApplicationViewModel
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}