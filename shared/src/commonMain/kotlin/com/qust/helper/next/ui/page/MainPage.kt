package com.qust.helper.next.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.viewmodel.EmptyViewModel


@Preview
@Composable
private fun MainPagePreview() = AppPreview(::MainUI)


class MainPage: AppPage<EmptyViewModel>(title = "主页", viewModelClass = EmptyViewModel::class) {
    @Composable
    override fun Content(viewModel: EmptyViewModel) = MainUI(viewModel)
}

@Composable
private fun MainUI(viewModel: EmptyViewModel) {

}

