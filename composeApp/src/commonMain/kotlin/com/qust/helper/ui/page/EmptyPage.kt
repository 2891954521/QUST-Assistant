package com.qust.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.viewmodel.BaseViewModel

object EmptyPage: BasePage<BaseViewModel>("", Icons.Default.Home) {

	@Composable
	override fun Content(viewModel: BaseViewModel) {
		Box(Modifier.fillMaxSize()) {
			Text("this page is empty", Modifier.align(Alignment.Center))
		}
	}

	@Composable
	override fun getViewModel() = viewModel<BaseViewModel>()
}