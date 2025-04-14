package com.qust.helper.ui.page

import android.content.Context
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.qust.helper.ui.activity.PageActivity

class AndroidPageController(
    val context: Context,
    private val dispatcher: OnBackPressedDispatcher?
): PageController {

    override fun startPage(key: String) {
        PageActivity.startActivity(context, key)
    }

    override fun back() {
        dispatcher?.onBackPressed()
    }

}

@Composable
actual fun rememberPageController(): PageController {
    val context = LocalContext.current
    val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    return remember { AndroidPageController(context, backDispatcherOwner?.onBackPressedDispatcher) }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}