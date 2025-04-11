package com.qust.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.qust.helper.ui.widget.toast.ToastUI
import com.qust.helper.viewmodel.BaseViewModel

abstract class BasePage<T: BaseViewModel>(
	val title: String,
	val icon: ImageVector,
) {

	val key: String = javaClass.name

	@Composable
	abstract fun getViewModel(): T

	@Composable
	fun BaseContent() {
		val viewModel = getViewModel()

		Scaffold { contentPadding ->
			Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
				Content(viewModel)

				ToastUI(viewModel.toastData, Modifier.align(Alignment.Center))
			}
		}
	}

	@Composable
	abstract fun Content(viewModel: T)
}