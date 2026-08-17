package com.qust.helper.next.ui.page.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.next.page.AbstractPage
import com.qust.helper.next.ui.component.overlay.OverlayProvider
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.theme.AppThemeProvider
import com.qust.helper.next.ui.viewmodel.AppViewModelFactory
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlin.reflect.KClass

abstract class BasePage<T: BaseViewModel>(
	title: String,
	val viewModelClass: KClass<T>,
): AbstractPage {

    var params: PageParam? = null

    val title by mutableStateOf(title)

    @Composable
    fun getViewModel(): T = viewModel(modelClass = viewModelClass, factory = AppViewModelFactory, extras = MutableCreationExtras().apply {
	    params?.let { set(PageParamKey, it) }
    })

    @Composable
    open fun PageContent() {
		BaseContent(getViewModel())
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    open fun PageContent(viewModel: BaseViewModel) {
        BaseContent(viewModel as T)
    }

    @Composable
    open fun BaseContent(viewModel: T) {
	    OverlayProvider(viewModel.overlay) {
		    Box(Modifier.statusBarsPadding()) {
			    Content(viewModel)
		    }
	    }
    }

    @Composable
    protected abstract fun Content(viewModel: T)

    object PageParamKey: CreationExtras.Key<PageParam>
}