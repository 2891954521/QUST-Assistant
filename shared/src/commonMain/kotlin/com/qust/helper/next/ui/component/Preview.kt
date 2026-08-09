package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.next.ui.component.overlay.OverlayEventBus
import com.qust.helper.next.ui.component.overlay.OverlayProvider
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import com.qust.helper.next.ui.theme.AppThemeProvider

@Composable
inline fun  AppPreview(crossinline content: @Composable () -> Unit){
    val overlayEvent = remember { OverlayEventBus() }
    AppThemeProvider {
        OverlayProvider(overlayEvent) {
            content()
        }
    }
}

@Composable
inline fun <reified T : BaseViewModel> AppPreview(crossinline content: @Composable (viewModel: T) -> Unit){
    val viewModel = viewModel<T>()
    AppThemeProvider {
        OverlayProvider(viewModel.overlay) {
                content(viewModel)
        }
    }
}