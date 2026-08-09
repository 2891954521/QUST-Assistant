package com.qust.helper.next.ui.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass

@Composable
actual fun rememberPlatformPageResultLauncher(
    router: BaseAppRouter,
    destination: KClass<out BasePage<*>>,
    resultKey: PageResultKey,
    onResult: (PageResult) -> Unit
): PageResultLauncher {
    val controller = router as DesktopPageController
    val caller = controller.screens.lastOrNull() ?: return PageResultLauncher.Empty

    val latestCallback by rememberUpdatedState(onResult)

    LaunchedEffect(caller.id, resultKey) {
        caller.resultStore.observe(resultKey).collect { result ->
            latestCallback(result)
            caller.resultStore.consume(resultKey)
        }
    }

    return remember(caller.id, destination, resultKey) {
        object: PageResultLauncher {
            override fun launch(block: (PageParamBuilder.() -> Unit)?) {
                controller.startPageForResult(clazz = destination, resultKey = resultKey, block = block)
            }
        }
    }
}