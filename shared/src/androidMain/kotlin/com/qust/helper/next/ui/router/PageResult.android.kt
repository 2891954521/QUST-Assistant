package com.qust.helper.next.ui.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass


private class PageActivityResultContract(
    private val destination: KClass<out BasePage<*>>
) : ActivityResultContract<Bundle, PageResult>() {

    override fun createIntent(context: Context, input: Bundle): Intent {
        return Intent(context, ActivityCollector.pageActivityClass).apply {
            putExtra("class", destination.java)
            putExtras(input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PageResult {
        if (resultCode == Activity.RESULT_CANCELED) {
            return PageResult.Canceled
        }

        if (resultCode != Activity.RESULT_OK) {
            return PageResult.Failure("未知页面返回码：$resultCode")
        }

        val content = intent?.extras ?: return PageResult.Failure("页面没有返回结果")

        return PageResult.Success(IntentPageParam(content))
    }
}

@Composable
actual fun rememberPlatformPageResultLauncher(
    router: BaseAppRouter,
    destination: KClass<out BasePage<*>>,
    resultKey: PageResultKey,
    onResult: (PageResult) -> Unit
): PageResultLauncher {
    val latestCallback by rememberUpdatedState(onResult)

    var inFlight by rememberSaveable(resultKey) { mutableStateOf(false) }

    val contract = remember(destination) { PageActivityResultContract(destination) }

    val launcher = rememberLauncherForActivityResult(contract) { result ->
        inFlight = false
        latestCallback(result)
    }

    return remember(launcher, resultKey) {
        object : PageResultLauncher {
            override fun launch(block: (PageParamBuilder.() -> Unit)?) {
                if(inFlight) {
                    Logger.w("PageResultKey '$resultKey' 已有未完成请求")
                    return
                }

                val builder = AndroidPageParamBuilder()
                block?.invoke(builder)

                inFlight = true

                launcher.launch(builder.build().bundle)
            }
        }
    }
}