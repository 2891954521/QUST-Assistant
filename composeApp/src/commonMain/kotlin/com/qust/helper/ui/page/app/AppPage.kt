package com.qust.helper.ui.page.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.web.AssetWebView
import com.qust.helper.viewmodel.BaseViewModel

object AppPage {

	/**
	 * 用户协议
	 */
	val UserAgreementPage = object : BasePage<BaseViewModel>("用户协议", Icons.Default.Info) {

		@Composable
		override fun getViewModel() = viewModel<BaseViewModel>()

		@Composable
		override fun Content(viewModel: BaseViewModel) {
			AssetWebView("userAgreement.html")
		}
	}

	/**
	 * 隐私政策
	 */
	val PolicyPage = object : BasePage<BaseViewModel>("隐私政策", Icons.Default.Lock) {

		@Composable
		override fun getViewModel() = viewModel<BaseViewModel>()

		@Composable
		override fun Content(viewModel: BaseViewModel) {
			AssetWebView("policy.html")
		}
	}
}
