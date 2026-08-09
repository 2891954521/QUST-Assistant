package com.qust.helper.next.ui.component.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.qust.helper.next.ui.component.overlay.dialog.DialogHost
import com.qust.helper.next.ui.component.overlay.loading.LoadingHost
import com.qust.helper.next.ui.component.overlay.toast.ToastHost


@Composable
fun OverlayProvider(eventBus: OverlayEventBus, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()

    val controller = remember(eventBus) { OverlayController(scope) }

    CompositionLocalProvider(
        LocalOverlayController provides controller,
        LocalToast provides controller
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            OverlayHost(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )

            OverlayEventCollector(controller, eventBus)
        }
    }
}


@Composable
private fun OverlayHost(
    controller: OverlayController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        DialogHost(
            controller = controller,
            modifier = Modifier.fillMaxSize().zIndex(OverlayZIndex.Dialog)
        )

        LoadingHost(
            controller = controller,
            modifier = Modifier.fillMaxSize().zIndex(OverlayZIndex.Loading)
        )

        ToastHost(
            controller = controller,
            modifier = Modifier.fillMaxSize().zIndex(OverlayZIndex.Toast)
        )
    }
}


@Composable
fun OverlayEventCollector(controller: OverlayController, eventBus: OverlayEventBus) {
    LaunchedEffect(eventBus, controller) {
        eventBus.events.collect { event ->
            when (event) {
                is OverlayEvent.Toast -> controller.toast(event.data)

                is OverlayEvent.Loading -> {
                    if (event.visible) {
                        controller.showLoading(event.text ?: "")
                    } else {
                        controller.dismissLoading()
                    }
                }
            }
        }
    }
}
