package com.qust.helper.ui.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.viewmodel.MyViewModel

object MyPage: BasePage<MyViewModel>("我的", Icons.Default.Settings) {

    @Composable
    override fun getViewModel() = viewModel<MyViewModel>()

    @Composable
    override fun Content(viewModel: MyViewModel) {

    }
}