package com.qust.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.qust.helper.ui.widget.LoadingUI
import com.qust.helper.ui.widget.layout.AppContentWithBack
import com.qust.helper.ui.widget.toast.ToastUI
import com.qust.helper.viewmodel.BaseViewModel

abstract class BasePage<T : BaseViewModel>(
	val title: String,
	val icon: ImageVector,
) {

	val key: String = javaClass.name

	@Composable
	abstract fun getViewModel(): T

	@Composable
	open fun ComposePage() {
		val pageController = rememberPageController()
		Scaffold { contentPadding ->
			AppContentWithBack(title = title, contentPadding = contentPadding, onBack = pageController::back) {
				BaseContent()
			}
		}
	}

	@Composable
	open fun BaseContent() {
		val viewModel = getViewModel()
		Box(modifier = Modifier.fillMaxSize()) {
			Content(viewModel)
			ToastUI(viewModel.toastData, Modifier.align(Alignment.Center))
			LoadingUI(viewModel)
		}
	}

	@Composable
	abstract fun Content(viewModel: T)
}