package com.qust.helper.next.ui.page.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qust.helper.next.App
import com.qust.helper.next.ui.component.layout.AppContentWithBack
import com.qust.helper.next.ui.component.overlay.OverlayProvider
import com.qust.helper.next.ui.theme.AppThemeProvider
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlin.reflect.KClass

abstract class AppPage<T: BaseViewModel>(
    title: String,
    val icon: ImageVector? = null,
    viewModelClass: KClass<T>
): BasePage<T>(
    title = title,
    viewModelClass = viewModelClass
) {

	@Composable
	override fun PageContent() {
		val router = App.router
		AppThemeProvider {
			AppContentWithBack(title, onBack = { router.back() }) {
				BaseContent(getViewModel())
			}
		}
	}

	@Composable
	@Suppress("UNCHECKED_CAST")
	override fun PageContent(viewModel: BaseViewModel) {
		val router = App.router
		AppThemeProvider {
			AppContentWithBack(title, onBack = { router.back() }) {
				BaseContent(viewModel as T)
			}
		}
	}

    @Composable
    override fun BaseContent(viewModel: T) {
	    OverlayProvider(viewModel.overlay) {
			Content(viewModel)
	    }
    }
}