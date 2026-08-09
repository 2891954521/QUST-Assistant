package com.qust.helper.next.ui.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.page.EmptyPage
import com.qust.helper.next.ui.page.PageRegistry
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass

sealed interface PageResult {
    data class Success(val pageParam: PageParam): PageResult, PageParam by pageParam
    data object Canceled: PageResult
    data class Failure(val message: String): PageResult
}

@JvmInline
value class PageResultKey(val value: String)

interface PageResultLauncher {

    companion object {
        val Empty = object: PageResultLauncher { }
    }

    fun launch(block: (PageParamBuilder.() -> Unit)? = null){ }
}

@Composable
inline fun <reified P : BasePage<*>> rememberLauncherForPageResult(key: PageResultKey, noinline onResult: (PageResult) -> Unit): PageResultLauncher {
    val router = LocalRouter.current
    val latestCallback by rememberUpdatedState(onResult)
    return rememberPlatformPageResultLauncher(router = router, destination = P::class, resultKey = key) { result ->
        latestCallback(result)
    }
}

@Composable
fun rememberLauncherForPageResult(key: PageResultKey, destination: String, onResult: (PageResult) -> Unit): PageResultLauncher {
    val router = LocalRouter.current
    val latestCallback by rememberUpdatedState(onResult)
    val page = PageRegistry.getPage(destination) ?: EmptyPage::class
    return rememberPlatformPageResultLauncher(router = router, destination = page, resultKey = key) { result ->
        latestCallback(result)
    }
}

@Composable
expect fun rememberPlatformPageResultLauncher(router: BaseAppRouter, destination: KClass<out BasePage<*>>, resultKey: PageResultKey, onResult: (PageResult) -> Unit): PageResultLauncher