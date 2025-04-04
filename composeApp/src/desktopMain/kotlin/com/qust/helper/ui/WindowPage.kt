package com.qust.helper.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.qust.helper.ui.page.BasePage
import com.qust.helper.viewmodel.ApplicationViewModel

class WindowPage(
    val page: BasePage<*>
) {

    @Composable
    fun Content() {
        Window(
            onCloseRequest = { ApplicationViewModel.close(this) },
            title = page.title,
        ) {
            page.BaseContent(PaddingValues())
        }
    }

    fun exit(){

    }
}