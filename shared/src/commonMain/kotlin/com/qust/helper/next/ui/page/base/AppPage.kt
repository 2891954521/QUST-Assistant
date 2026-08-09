package com.qust.helper.next.ui.page.base

import androidx.compose.runtime.Composable
import com.qust.helper.next.ui.component.overlay.OverlayProvider
import com.qust.helper.next.ui.theme.AppThemeProvider
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlin.reflect.KClass

abstract class AppPage<T: BaseViewModel>(
    title: String,
    viewModelClass: KClass<T>
): BasePage<T>(
    title = title,
    viewModelClass = viewModelClass
) {

    @Composable
    override fun BaseContent(viewModel: T) {
	    AppThemeProvider {
		    OverlayProvider(viewModel.overlay) {
				Content(viewModel)
		    }
	    }
    }
}