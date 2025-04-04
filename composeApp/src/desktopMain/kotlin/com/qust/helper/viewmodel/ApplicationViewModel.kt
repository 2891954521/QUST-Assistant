package com.qust.helper.viewmodel

import androidx.compose.runtime.mutableStateListOf
import com.qust.helper.data.Pages
import com.qust.helper.ui.WindowPage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.page.HomePage
import com.qust.helper.ui.page.PageController

object ApplicationViewModel: PageController {

    private val _windows = mutableStateListOf(
        WindowPage(HomePage)
    )

    val windows: List<WindowPage> get() = _windows

    override fun startPage(key: String) {
        val page = Pages[key] ?: EmptyPage
        _windows.add(
            WindowPage(
                page = page,
            )
        )
    }

    override fun back() {
        val item = _windows.last()
        item.exit()
        _windows.removeLast()
    }


    fun close(windowPage: WindowPage){
        windowPage.exit()
        _windows.remove(windowPage)
    }
}