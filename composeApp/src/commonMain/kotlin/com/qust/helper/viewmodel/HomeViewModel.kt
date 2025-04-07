package com.qust.helper.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.qust.helper.data.Pages
import com.qust.helper.ui.page.BasePage

class HomeViewModel: BaseViewModel() {

    val pages: List<BasePage<*>> by mutableStateOf(Pages.defaultPages)

}