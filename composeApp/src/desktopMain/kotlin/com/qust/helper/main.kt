package com.qust.helper

import androidx.compose.runtime.key
import androidx.compose.ui.window.application
import com.qust.helper.ui.theme.AppTheme
import com.qust.helper.viewmodel.ApplicationViewModel

fun main() = application {
    AppTheme {
        for (window in ApplicationViewModel.windows) {
            key(window) {
                window.Content()
            }
        }
    }
}