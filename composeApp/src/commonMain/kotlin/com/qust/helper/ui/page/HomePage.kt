package com.qust.helper.ui.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.viewmodel.HomeViewmodel

object HomePage: BasePage<HomeViewmodel>("", Icons.Default.Home) {

	@Composable
	override fun Content(viewModel: HomeViewmodel) {
		TODO("Not yet implemented")
	}

	@Composable
	override fun getViewModel() = viewModel<HomeViewmodel>()
}