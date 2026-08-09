package com.qust.helper.next.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.qust.helper.next.ui.component.Windows
import com.qust.helper.next.ui.router.DesktopPageController
import com.qust.helper.next.ui.router.LocalRouter
import com.qust.helper.next.ui.theme.AppThemeProvider

/**
 * 当前的屏幕，相当于 Android 的 LocalActivity
 */
val LocalScreen = staticCompositionLocalOf<Screen> { error("current screen is null") }

fun gui(){
    application {
        val state = rememberWindowState()

        var pixelsize by remember { mutableStateOf(IntSize(1920, 1080)) }
        var dpsize by remember { mutableStateOf(DpSize(1920.dp, 1080.dp)) }

        val currentScreen = DesktopPageController.screens.lastOrNull()

        Window(
            onCloseRequest = {
                DesktopPageController.askForExit = true
            },
            title = currentScreen?.page?.title ?: "",
            state = state
        ){
            Box(Modifier.onSizeChanged {
                pixelsize = it
                dpsize = DpSize(it.width.dp, it.height.dp)
            }){
                if(currentScreen == null) return@Box

                DesktopCompositionLocalProvider(pixelsize, dpsize, currentScreen){
                    key(currentScreen) {
                        currentScreen.page.PageContent()
                    }
                }
            }

            if(DesktopPageController.askForExit){
                ExitAppDialog(onDismiss = {
                    DesktopPageController.askForExit = false
                }, onConfirm = {
                    DesktopPageController.finishAllPage()
                    exitApplication()
                })
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun DesktopCompositionLocalProvider(
    pixelSize: IntSize,
    dpSize: DpSize,
    screen: Screen,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides screen,

        LocalScreen provides screen,
        LocalRouter provides DesktopPageController,

        Windows.LocalWindowsPixels provides pixelSize,
        Windows.LocalWindowsDpSize provides dpSize,
        Windows.LocalWindowsSize provides WindowSizeClass.calculateFromSize(dpSize),
        content = content
    )
}

@Composable
private fun ExitAppDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppThemeProvider {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("退出应用")
            },
            text = {
                Text("是否退出应用")
            },
            confirmButton = {
                TextButton(onClick = onConfirm, content = {
                    Text("确定", color = Color.Red)
                })
            },
            dismissButton = {
                TextButton(onClick = onDismiss, content = {
                    Text("取消", color = Color.DarkGray)
                })
            }
        )
    }
}